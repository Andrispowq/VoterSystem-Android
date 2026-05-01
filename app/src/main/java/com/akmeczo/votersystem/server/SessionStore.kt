package com.akmeczo.votersystem.server

import android.content.Context
import android.util.Base64
import com.akmeczo.votersystem.server.responses.TokensDto
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit

class SessionStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveTokens(tokens: TokensDto) {
        val payload = StoredSession(
            authToken = tokens.authToken,
            refreshToken = tokens.refreshToken.toString(),
            userId = tokens.userId.toString()
        )

        preferences.edit {
            putString(SESSION_KEY, encrypt(ApiJson.encode(payload)))
        }
    }

    fun getTokens(): TokensDto? {
        val encryptedSession = preferences.getString(SESSION_KEY, null) ?: return null
        val decryptedSession = runCatching { decrypt(encryptedSession) }.getOrNull() ?: return null
        val storedSession = runCatching { ApiJson.decode<StoredSession>(decryptedSession) }.getOrNull() ?: return null

        return runCatching {
            TokensDto(
                authToken = storedSession.authToken,
                refreshToken = UUID.fromString(storedSession.refreshToken),
                userId = UUID.fromString(storedSession.userId)
            )
        }.getOrNull()
    }

    fun clear() {
        preferences.edit { remove(SESSION_KEY) }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        val combined = cipher.iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val decoded = Base64.decode(value, Base64.NO_WRAP)
        val iv = decoded.copyOfRange(0, IV_LENGTH_BYTES)
        val encrypted = decoded.copyOfRange(IV_LENGTH_BYTES, decoded.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))

        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private data class StoredSession(
        val authToken: String,
        val refreshToken: String,
        val userId: String
    )

    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "voter_system_session_key"
        private const val PREFERENCES_NAME = "secure_session_store"
        private const val SESSION_KEY = "session"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH_BYTES = 12
        private const val TAG_LENGTH_BITS = 128
    }
}
