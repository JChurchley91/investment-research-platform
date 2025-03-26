package config

import api.CoinPricesTask
import api.DailyNewsTask
import api.SharePricesTask
import models.ApiResponses
import models.DailyCoinPrices
import models.DailySharePrices
import models.TrendingNewsArticles

val schemas =
    listOf(
        "raw",
    )

val databaseTables =
    listOf(
        ApiResponses,
        TrendingNewsArticles,
        DailyCoinPrices,
        DailySharePrices,
    )

val sharePriceTickers =
    listOf(
        "AAPL",
        "MSFT",
        "NVDA",
        "AMZN",
        "AVGO",
    )

val cryptoCoins =
    listOf(
        "BTC",
        "ETH",
        "ADA",
        "XRP",
        "SOL",
    )

val tasksToSchedule =
    listOf(
        Triple(
            DailyNewsTask()::callApi,
            DailyNewsTask()::taskSchedule,
            DailyNewsTask()::taskName,
        ),
        Triple(
            CoinPricesTask()::callApi,
            CoinPricesTask()::taskSchedule,
            CoinPricesTask()::taskName,
        ),
        Triple(
            SharePricesTask()::callApi,
            SharePricesTask()::taskSchedule,
            SharePricesTask()::taskName,
        ),
    )
