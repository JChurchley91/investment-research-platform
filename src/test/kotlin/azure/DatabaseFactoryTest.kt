package azure

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Test class for DatabaseFactory.
 * Test if the DatabaseFactory is initialized correctly.
 */
class DatabaseFactoryTest :
    FunSpec({
        test("DatabaseFactory should be initialized correctly") {
            DatabaseFactory.init() shouldBe true
        }
    })
