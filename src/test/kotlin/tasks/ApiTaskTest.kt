package tasks

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ApiTaskTest :
    FunSpec({
        lateinit var testApiTask: ApiTask

        beforeTest {
            testApiTask =
                ApiTask(
                    taskName = "Coin Prices",
                    taskSchedule = "* * * * *",
                    apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,litecoin&vs_currencies=usd",
                )
        }

        test("ApiTask should be initialized correctly") {
            testApiTask.taskName shouldBe "Coin Prices"
            testApiTask.taskSchedule shouldBe "* * * * *"
            testApiTask.apiUrl shouldBe "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum,litecoin&vs_currencies=usd"
        }
    })
