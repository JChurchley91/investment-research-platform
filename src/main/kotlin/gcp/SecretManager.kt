package gcp

import com.google.cloud.secretmanager.v1.*

class SecretManager {
    val projectId = "538717258361"

    fun getSecret(secretId: String): String {
        SecretManagerServiceClient.create().use { client ->
            val secretName = SecretName.of(projectId, secretId)
            val secretVersion = client.accessSecretVersion("$secretName/versions/1")
            val payload = secretVersion.payload.data.toStringUtf8()
            return payload
        }
    }
}
