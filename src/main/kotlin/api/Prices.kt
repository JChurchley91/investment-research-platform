package api

import gcp.SecretManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.math.round
import kotlin.text.get
import kotlin.times

class Prices {
    val taskName: String = "Prices"
    val taskSchedule: String = "* * * * *"
    val today: LocalDate = LocalDate.now()
    val defaultJson =
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    private val logger = LoggerFactory.getLogger(Prices::class.java)
    private val apiKey = SecretManager().getSecret("coinmarketcap-api-key")
    private val cryptoCoins = listOf("BTC", "ETH", "ADA", "XRP")
    private val apiUrl: String = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/quotes/latest"

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
        @SerialName("last_updated") val lastUpdated: String,
    )

    suspend fun callApi() =
        try {
            logger.info("Calling API; Fetching Prices")
            val client =
                HttpClient(CIO) {
                }

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
                    val lastUpdated: String = gbpData.lastUpdated
                } else {
                    logger.warn("GBP data not found for $coin")
                }
            }
        } catch (exception: Exception) {
            logger.error("Error calling API: $exception")
        }
}
