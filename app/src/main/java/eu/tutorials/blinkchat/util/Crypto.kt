package eu.tutorials.blinkchat.util

import android.util.Base64
import com.google.gson.Gson
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Meeting
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object Crypto {
    private const val ENCRYPTION_ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES"
    private val gson = Gson()

    // Generate a new AES key (useful for testing or generating keys securely)
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM)
        keyGenerator.init(128) // Use 128-bit key (16 bytes)
        return keyGenerator.generateKey()
    }

    // Convert SecretKey to String
    fun keyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    // Convert String back to SecretKey with length adjustment
    fun stringToKey(keyString: String): SecretKey {
        return adjustKeyLength(keyString)
    }

    // Adjust key length to 16 bytes (128 bits) for AES
    private fun adjustKeyLength(keyString: String): SecretKey {
        val decodedKey = Base64.decode(keyString, Base64.DEFAULT)
        val adjustedKey = when {
            decodedKey.size > 16 -> decodedKey.copyOfRange(0, 16) // Truncate to 16 bytes
            decodedKey.size < 16 -> decodedKey.copyOf(16) // Pad with zeros
            else -> decodedKey // Correct size
        }
        return SecretKeySpec(adjustedKey, ENCRYPTION_ALGORITHM)
    }

    // Encrypt data
    fun encrypt(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    // Decrypt data
    fun decrypt(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decodedBytes = Base64.decode(data, Base64.DEFAULT)
        return String(cipher.doFinal(decodedBytes))
    }

    // Encrypt a Contact object
    fun encryptContact(contact: Contact, secretKey: SecretKey): String {
        val contactJson = gson.toJson(contact)
        return encrypt(contactJson, secretKey)
    }

    // Decrypt a Contact object
    fun decryptContact(encryptedContact: String, secretKey: SecretKey): Contact {
        val decryptedJson = decrypt(encryptedContact, secretKey)
        return gson.fromJson(decryptedJson, Contact::class.java)
    }

    fun encryptMeeting(meeting: Meeting, secretKey: SecretKey): String {
        val meetingJson = gson.toJson(meeting)
        return encrypt(meetingJson, secretKey)
    }

    // Decrypt a Meeting object
    fun decryptMeeting(encryptedMeeting: String, secretKey: SecretKey): Meeting {
        val decryptedJson = decrypt(encryptedMeeting, secretKey)
        return gson.fromJson(decryptedJson, Meeting::class.java)
    }
}
