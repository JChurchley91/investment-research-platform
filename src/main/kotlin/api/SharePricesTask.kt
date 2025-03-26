package api

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
import java.time.LocalDateTime

class SharePricesTask :
    ApiTask(
        taskName = "sharePrices",
        taskSchedule = "0 9 * * *",
        apiKeyName = "alpha-vantage-key",
        apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY",
    ) {
    val tickers =
        listOf(
            "AAPL",
            "MSFT",
            "NVDA",
            "AMZN",
            "AVGO",
            "META",
            "NFLX",
            "COST",
            "GOOGL",
            "TSLA",
        )

    @Serializable
    data class TimeSeriesDaily(
        @SerialName("Time Series (Daily)") val timeSeriesDaily: Map<String, JsonObject>,
    )

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
            it[apiResponseKey] = "$ticker-$yesterday"
            it[task] = taskName
            it[open] = openValue
            it[high] = highValue
            it[low] = lowValue
            it[close] = closeValue
            it[volume] = volumeValue
            it[createdAt] = LocalDateTime.now()
        }
    }

    suspend fun callApi() {
        logger.info("Calling API; Fetching Prices")
        for (ticker in tickers) {
            logger.info("Fetching prices for $ticker; $yesterday")
            val httpResponse: HttpResponse = client.get("$apiUrl&symbol=$ticker&apikey=$apiKey")
            val responseBody: String = httpResponse.body()
            val timeSeriesDaily: TimeSeriesDaily = defaultJson.decodeFromString(responseBody)
            val timeSeriesDailyToday = timeSeriesDaily.timeSeriesDaily[yesterday.toString()]

            if (timeSeriesDailyToday != null) {
                val openValue: Double = timeSeriesDailyToday["1. open"]!!.jsonPrimitive.double
                val highValue: Double = timeSeriesDailyToday["2. high"]!!.jsonPrimitive.double
                val lowValue: Double = timeSeriesDailyToday["3. low"]!!.jsonPrimitive.double
                val closeValue: Double = timeSeriesDailyToday["4. close"]!!.jsonPrimitive.double
                val volumeValue: Double = timeSeriesDailyToday["5. volume"]!!.jsonPrimitive.double

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
