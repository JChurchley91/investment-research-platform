package models

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import models.api_extracts.ApiResponses
import models.api_extracts.DailyCoinPrices
import models.api_extracts.DailyNewsArticles
import models.api_extracts.DiffbotExtract
import models.api_extracts.DailyNewsScreenshots
import models.api_extracts.DailyTopNews

/**
 * Test class for Models.
 * Test if the models return the correct table names.
 */
class ModelsTest :
    FunSpec({
        lateinit var api: ApiResponses
        lateinit var dailyNews: DailyNewsArticles
        lateinit var dailyCoin: DailyCoinPrices
        lateinit var diffbot: DiffbotExtract

        beforeTest {
            api = ApiResponses
            dailyNews = DailyNewsArticles
            dailyCoin = DailyCoinPrices
            diffbot = DiffbotExtract
        }

        test("ApiResponses should have the correct table name") {
            api.tableName shouldBe "raw.api_responses"
        }

        test("DailyNewsArticles should have the correct table name") {
            dailyNews.tableName shouldBe "raw.daily_news_articles"
        }

        test("DailyCoinPrices should have the correct table name") {
            dailyCoin.tableName shouldBe "raw.daily_coin_prices"
        }

        test("DiffbotExtract should have the correct table name") {
            diffbot.tableName shouldBe "raw.diffbot_extracts"
        }

        test("DailyNewsScreenshots should have the correct table name") {
            DailyNewsScreenshots.tableName shouldBe "raw.daily_news_screenshots"
        }

        test("DailyTopNews should have the correct table name") {
            DailyTopNews.tableName shouldBe "raw.daily_top_news"
        }
    })
