import api.TrendingNews
import scheduler.Scheduler

fun main() {
    try {
        val trendingNews = TrendingNews()
        trendingNews.initialize()

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
    } catch (exception: Exception) {
        println("Error occured during application runtime: $exception")
    }
}
