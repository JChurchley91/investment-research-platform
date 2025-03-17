import api.TrendingNews
import scheduler.Scheduler

fun main() {
    val trendingNews = TrendingNews()
    val scheduler = Scheduler()

    scheduler.start(
        listOf(
            Triple(
                trendingNews::callApi,
                trendingNews::taskSchedule,
                trendingNews::taskName,
            ),
        ),
    )
}
