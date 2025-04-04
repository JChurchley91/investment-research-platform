package config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Test class for AppConfig.
 * Test if the AppConfig returns the correct schemas, database tables,
 * share price tickers, coins, and tasks to schedule.
 */
class AppConfigTest :
    FunSpec({
        lateinit var appConfig: AppConfig
        beforeTest {
            appConfig = AppConfig()
        }
        test("AppConfig should return the correct schemas") {
            val expectedSchemas = listOf("raw", "enhanced")
            appConfig.getSchemas() shouldBe expectedSchemas
        }
        test("AppConfig should return the correct database tables") {
            val expectedTables =
                listOf(
                    "raw.api_responses",
                    "raw.daily_news_articles",
                    "raw.daily_coin_prices",
                    "raw.diffbot_extracts",
                    "raw.daily_news_screenshots",
                    "raw.daily_top_news",
                )
            appConfig.getDatabaseTables().map { it.tableName } shouldBe expectedTables
        }
        test("AppConfig should return the correct crypto coins") {
            val expectedCoins = listOf("BTC", "ETH", "ADA", "XRP", "SOL", "DOT")
            appConfig.getCryptoCoins() shouldBe expectedCoins
        }
        test("AppConfig should return the correct tasks to schedule") {
            val expectedTasks =
                listOf(
                    "coinPrices",
                    "dailyNewsSearch",
                    "diffbotExtract",
                    "dailyNewsScreenshot",
                    "dailyTopNews",
                )
            val returnedTaskNames = appConfig.getTasksToSchedule().map { it.taskName.get() }
            returnedTaskNames shouldBe expectedTasks
        }
    })
