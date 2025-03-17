package azure

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder

object KeyVaultClient {
    private const val KEY_VAULT_URL = "https://astrocatdevkeyvault.vault.azure.net/"
    private val secretClient: SecretClient =
        SecretClientBuilder()
            .vaultUrl(KEY_VAULT_URL)
            .credential(DefaultAzureCredentialBuilder().build())
            .buildClient()

    fun getSecret(secretName: String): String = secretClient.getSecret(secretName).value
}
