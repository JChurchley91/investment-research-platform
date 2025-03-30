package tasks.api_tasks

import azure.SecretManager
import config.AppConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import models.api_extracts.DailyCoinPrices
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Task to fetch and store daily coin prices from the Alpha Vantage API.
 */
class CoinPricesTask :
    ApiTask(
        taskName = "coinPrices",
        taskSchedule = "* 9 * * *",
        apiUrl = "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_DAILY",
    ) {
    val appConfig: AppConfig = AppConfig()
    val cryptoCoins: List<String> = appConfig.getCryptoCoins()
    val market: String = "USD"
    val apiKeyName = "alpha-vantage-key"

    /**
     * Data class representing the response from the Alpha Vantage API.
     */
    @Serializable
    data class TimeSeriesDaily(
        @SerialName("Time Series (Digital Currency Daily)") val timeSeriesDaily: Map<String, JsonObject>,
    )

    /**
     * Inserts the coin prices API response into the database.
     *
     * @param coin The name of the coin.
     * @param openValue The opening price of the coin.
     * @param highValue The highest price of the coin.
     * @param lowValue The lowest price of the coin.
     * @param closeValue The closing price of the coin.
     * @param volumeValue The trading volume of the coin.
     */
    fun insertCoinPrices(
        coin: String,
        openValue: Double,
        highValue: Double,
        lowValue: Double,
        closeValue: Double,
        volumeValue: Double,
    ) {
        logger.info("Inserting coin price data for $coin; $yesterday")
        DailyCoinPrices.insert {
            it[apiResponseKey] = "$coin-$today"
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
     * Calls the Alpha Vantage API to fetch daily coin prices.
     * Stores the response in the database.
     */
    suspend fun callApi() {
        logger.info("Calling API; Fetching Prices")
        val secretManager = SecretManager()
        val apiKey: String? = secretManager.getSecret(apiKeyName)
        for (coin in cryptoCoins) {
            logger.info("Fetching prices for $coin; $yesterday")
            val httpResponse: HttpResponse = client.get("$apiUrl&symbol=$coin&market=$market&apikey=$apiKey")
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
                    if (checkExistingApiResponse(coin)) {
                        logger.info("Data already exists for $coin on $yesterday")
                        return@transaction
                    } else {
                        insertApiResponse(coin, httpResponse)
                        insertCoinPrices(coin, openValue, highValue, lowValue, closeValue, volumeValue)
                    }
                }
            } else {
                logger.error("No data found for $coin on $yesterday")
            }
        }
    }
}
