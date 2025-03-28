package azure

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DatabaseFactoryTest :
    FunSpec({
        test("DatabaseFactory should be initialized correctly") {
            DatabaseFactory.init() shouldBe true
        }
    })
