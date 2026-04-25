package com.qrzen.app.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ITERATIONS = 10_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val HASH_LENGTH = KEY_LENGTH / 8

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        val derivedHash = deriveKey(password, salt)
        val encoder = Base64.getEncoder()
        return "${encoder.encodeToString(salt)}:${encoder.encodeToString(derivedHash)}"
    }

    fun verify(password: String, storedHash: String): Boolean {
        if (!isHashed(storedHash)) return false
        val parts = storedHash.split(":", limit = 2)
        val decoder = Base64.getDecoder()
        val salt = decoder.decode(parts[0])
        val expectedHash = decoder.decode(parts[1])
        val actualHash = deriveKey(password, salt)
        return MessageDigest.isEqual(actualHash, expectedHash)
    }

    fun isHashed(value: String): Boolean {
        if (value.count { it == ':' } != 1) return false
        val parts = value.split(":", limit = 2)
        return try {
            val decoder = Base64.getDecoder()
            val salt = decoder.decode(parts[0])
            val hash = decoder.decode(parts[1])
            salt.size == SALT_LENGTH && hash.size == HASH_LENGTH
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return try {
            SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }
}
