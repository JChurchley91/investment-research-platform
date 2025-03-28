package azure

import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import io.github.cdimascio.dotenv.dotenv

class SecretManager {
    private val dotenv = dotenv()
    private val keyVaultName =
        dotenv["AZURE_KEY_VAULT_NAME"]
            ?: throw IllegalArgumentException("AZURE_KEY_VAULT_NAME is not set in .env file")
    private val keyVaultUri = "https://$keyVaultName.vault.azure.net"
    private val client: SecretClient

    init {
        val clientId = dotenv["AZURE_CLIENT_ID"]
        val clientSecret = dotenv["AZURE_CLIENT_SECRET"]
        val tenantId = dotenv["AZURE_TENANT_ID"]

        client =
            SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(
                    ClientSecretCredentialBuilder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .tenantId(tenantId)
                        .build(),
                ).buildClient()
    }

    fun getSecret(secretName: String): String {
        val secret = client.getSecret(secretName)
        return secret.value
    }
}
