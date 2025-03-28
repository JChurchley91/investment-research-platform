package config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppConfigTest :
    FunSpec({
        lateinit var appConfig: AppConfig
        beforeTest {
            appConfig = AppConfig()
        }
        test("AppConfig should return the correct schemas") {
            val expectedSchemas = listOf("raw")
            appConfig.getSchemas() shouldBe expectedSchemas
        }
        test("AppConfig should return the correct database tables") {
            val expectedTables =
                listOf(
                    "raw.api_responses",
                    "raw.daily_news_articles",
                    "raw.daily_coin_prices",
                    "raw.daily_share_prices",
                    "raw.diffbot_extracts",
                )
            appConfig.getDatabaseTables().map { it.tableName } shouldBe expectedTables
        }
        test("AppConfig should return the correct share price tickers") {
            val expectedTickers = listOf("AAPL", "GOOGL", "MSFT", "AMZN", "META")
            appConfig.getSharePriceTickers() shouldBe expectedTickers
        }
        test("AppConfig should return the correct crypto coins") {
            val expectedCoins = listOf("BTC", "ETH", "ADA", "XRP", "SOL")
            appConfig.getCryptoCoins() shouldBe expectedCoins
        }
        test("AppConfig should return the correct tasks to schedule") {
            val expectedTasks =
                listOf(
                    "coinPrices",
                    "sharePrices",
                    "dailyNewsSearch",
                    "diffbotExtract",
                )
            val returnedTaskNames = appConfig.getTasksToSchedule().map { it.taskName.get() }
            returnedTaskNames shouldBe expectedTasks
        }
    })
