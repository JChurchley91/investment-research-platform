package api

import config.cryptoCoins
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.DailyCoinPrices
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.round

class CoinPricesTask :
    ApiTask(
        taskName = "coinPrices",
        taskSchedule = "0 9 * * *",
        apiKeyName = "coinmarketcap-api-key",
        apiUrl = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest",
    ) {
    val today: LocalDate = LocalDate.now()

    @Serializable
    data class PriceData(
        val data: Map<String, CoinData>,
    )

    @Serializable
    data class CoinData(
        val quote: Map<String, GBP>,
    )

    @Serializable
    data class GBP(
        val price: Double,
        @SerialName("percent_change_1h") val percentChange24H: Double,
    )

    fun insertCoinPrices(
        coin: String,
        price: Double,
        percentChange24H: Double,
    ) {
        logger.info("Inserting coin price data for $coin; $today")
        DailyCoinPrices.insert {
            it[apiResponseKey] = "$coin-$today"
            it[task] = taskName
            it[dailyCoinPrice] = price
            it[percentageChange24H] = percentChange24H
            it[createdAt] = LocalDateTime.now()
        }
    }

    suspend fun callApi() =
        try {
            logger.info("Calling API; Fetching Prices")

            for (coin in cryptoCoins) {
                logger.info("Fetching prices for $coin; $today")
                val httpResponse: HttpResponse =
                    client.get(apiUrl) {
                        parameter("symbol", coin)
                        parameter("convert", "GBP")
                        header("X-CMC_PRO_API_KEY", apiKey)
                    }
                val responseBody: String = httpResponse.body()
                val priceData: PriceData = defaultJson.decodeFromString(responseBody)
                val gbpData: GBP? = priceData.data[coin]?.quote?.get("GBP")

                if (gbpData != null) {
                    val price: Double = round(gbpData.price * 100) / 100
                    val percentChange24H: Double = round(gbpData.percentChange24H * 100) / 100

                    transaction {
                        if (checkExistingApiResponse(coin)) {
                            logger.info("API Response Already Exists; $coin-$today-$taskName")
                            return@transaction
                        } else {
                            insertApiResponse(coin, httpResponse)
                            insertCoinPrices(coin, price, percentChange24H)
                        }
                    }
                } else {
                    logger.warn("GBP data not found for $coin")
                }
            }
        } catch (exception: Exception) {
            logger.error("Error calling API: $exception")
        }
}
