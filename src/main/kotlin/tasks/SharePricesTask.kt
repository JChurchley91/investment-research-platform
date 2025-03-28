package tasks

import azure.SecretManager
import config.AppConfig
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import models.DailySharePrices
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Task to fetch and store daily share prices from the Alpha Vantage API.
 */
class SharePricesTask :
    ApiTask(
        taskName = "sharePrices",
        taskSchedule = "5 9 * * *",
        apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY",
    ) {
    val appConfig: AppConfig = AppConfig()
    val sharePriceTickers: List<String> = appConfig.getSharePriceTickers()
    val apiKeyName: String = "alpha-vantage-key"

    /**
     * Data class representing the response from the Alpha Vantage API.
     */
    @Serializable
    data class TimeSeriesDaily(
        @SerialName("Time Series (Daily)") val timeSeriesDaily: Map<String, JsonObject>,
    )

    /**
     * Inserts the share prices API response into the database.
     *
     * @param ticker The ticker symbol of the share.
     * @param openValue The opening price of the share.
     * @param highValue The highest price of the share.
     * @param lowValue The lowest price of the share.
     * @param closeValue The closing price of the share.
     * @param volumeValue The trading volume of the share.
     */
    fun insertSharePrices(
        ticker: String,
        openValue: Double,
        highValue: Double,
        lowValue: Double,
        closeValue: Double,
        volumeValue: Double,
    ) {
        logger.info("Inserting share price data for $ticker; $yesterday")
        DailySharePrices.insert {
            it[apiResponseKey] = "$ticker-$today"
            it[task] = taskName
            it[open] = openValue
            it[high] = highValue
            it[low] = lowValue
            it[close] = closeValue
            it[volume] = volumeValue
            it[createdAt] = today
        }
    }

    /**
     * Call the Alpha Vantage API to fetch share prices.
     */
    suspend fun callApi() {
        logger.info("Calling API; Fetching Share Prices")
        val secretManager = SecretManager()
        val apiKey: String = secretManager.getSecret(apiKeyName)
        for (ticker in sharePriceTickers) {
            logger.info("Fetching prices for $ticker; $yesterday")
            val httpResponse: HttpResponse = client.get("$apiUrl&symbol=$ticker&apikey=$apiKey")
            val responseBody: String = httpResponse.body()
            val timeSeriesDaily: TimeSeriesDaily = defaultJson.decodeFromString(responseBody)
            val timeSeriesDailyYesterday = timeSeriesDaily.timeSeriesDaily[yesterday.toString()]

            if (timeSeriesDailyYesterday != null) {
                val openValue: Double = timeSeriesDailyYesterday["1. open"]!!.jsonPrimitive.double
                val highValue: Double = timeSeriesDailyYesterday["2. high"]!!.jsonPrimitive.double
                val lowValue: Double = timeSeriesDailyYesterday["3. low"]!!.jsonPrimitive.double
                val closeValue: Double = timeSeriesDailyYesterday["4. close"]!!.jsonPrimitive.double
                val volumeValue: Double = timeSeriesDailyYesterday["5. volume"]!!.jsonPrimitive.double

                transaction {
                    if (checkExistingApiResponse(ticker)) {
                        logger.info("Data already exists for $ticker on $yesterday")
                        return@transaction
                    } else {
                        insertApiResponse(ticker, httpResponse)
                        insertSharePrices(ticker, openValue, highValue, lowValue, closeValue, volumeValue)
                    }
                }
            } else {
                logger.error("No data found for $ticker on $yesterday")
            }
        }
    }
}
