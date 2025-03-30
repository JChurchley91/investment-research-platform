package scheduler

import config.TaskConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import tasks.api_tasks.CoinPricesTask

/**
 * Test class for Scheduler.
 * Test if the Scheduler is initialized correctly.
 */
class SchedulerTest :
    FunSpec({

        lateinit var taskConfig: TaskConfig
        lateinit var scheduler: Scheduler
        lateinit var testApiTask: CoinPricesTask

        beforeTest {
            scheduler = Scheduler()
            testApiTask = CoinPricesTask()
            taskConfig =
                TaskConfig(
                    testApiTask::callApi,
                    testApiTask::taskName,
                    testApiTask::taskSchedule,
                    testApiTask::cryptoCoins,
                )
        }

        test("Scheduler should be initialized correctly") {
            val tasksToSchedule = listOf(taskConfig)
            runBlocking {
                scheduler.start(tasksToSchedule) shouldBe true
            }
        }
    })
