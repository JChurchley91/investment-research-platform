package azure

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder

class SecretManager {
    private val keyVaultName = "astrocatdevkeyvault"
    private val keyVaultUri = "https://$keyVaultName.vault.azure.net"
    private val client: SecretClient = SecretClientBuilder()
        .vaultUrl(keyVaultUri)
        .credential(DefaultAzureCredentialBuilder().build())
        .buildClient()

    fun getSecret(secretName: String): String {
        val secret = client.getSecret(secretName)
        return secret.value
    }
}