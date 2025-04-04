package config

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import tasks.api_tasks.CoinPricesTask

/**
 * Test class for TaskConfig.
 * Test if the TaskConfig returns the correct task name, schedule, function, and parameters.
 */
class TaskConfigTest :
    FunSpec({
        lateinit var taskConfig: TaskConfig
        lateinit var coinPricesTask: CoinPricesTask

        beforeTest {
            coinPricesTask = CoinPricesTask()
            taskConfig =
                TaskConfig(
                    coinPricesTask::callApi,
                    coinPricesTask::taskName,
                    coinPricesTask::taskSchedule,
                    coinPricesTask::cryptoCoins,
                )
        }

        test("TaskConfig should return the correct task name") {
            val expectedTaskName = "coinPrices"
            taskConfig.taskName.get() shouldBe expectedTaskName
        }

        test("TaskConfig should return the correct task schedule") {
            val expectedTaskSchedule = "30 7 * * *"
            taskConfig.taskSchedule.get() shouldBe expectedTaskSchedule
        }

        test("TaskConfig should return the correct task function") {
            val expectedTaskFunction = coinPricesTask::callApi
            taskConfig.taskFunction shouldBe expectedTaskFunction
        }

        test("TaskConfig should return the correct task parameters") {
            val expectedTaskParameters = listOf("BTC", "ETH", "ADA", "XRP", "SOL", "DOT")
            taskConfig.taskParameters.get() shouldBe expectedTaskParameters
        }
    })
