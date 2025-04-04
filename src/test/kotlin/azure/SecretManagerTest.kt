package azure

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * Test class for SecretManager.
 * Test if the SecretManager returns the correct secret.
 */
class SecretManagerTest :
    FunSpec({
        lateinit var secretManager: SecretManager
        beforeTest {
            secretManager = SecretManager()
        }
        test("SecretManager should return a test secret") {
            secretManager.getSecret("test-secret") shouldBe "test-secret-value"
        }
    })
