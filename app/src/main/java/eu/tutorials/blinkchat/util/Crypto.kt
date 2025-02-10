package eu.tutorials.blinkchat.util

import android.util.Base64
import com.google.gson.Gson
import eu.tutorials.blinkchat.data.model.Contact
import eu.tutorials.blinkchat.data.model.Meeting
import eu.tutorials.blinkchat.data.model.Message
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Crypto {
    private const val ENCRYPTION_ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val IV_SIZE = 16 // AES block size (16 bytes for AES)
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

    // Generate a random IV
    private fun generateIv(): IvParameterSpec {
        val iv = ByteArray(IV_SIZE)
        java.security.SecureRandom().nextBytes(iv)
        return IvParameterSpec(iv)
    }

    // Encrypt data with random IV
    fun encrypt(data: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivSpec = generateIv()
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = cipher.doFinal(data.toByteArray())

        // Prepend the IV to the encrypted data for later decryption
        val ivAndEncryptedData = ByteArray(IV_SIZE + encryptedBytes.size)
        System.arraycopy(ivSpec.iv, 0, ivAndEncryptedData, 0, IV_SIZE)
        System.arraycopy(encryptedBytes, 0, ivAndEncryptedData, IV_SIZE, encryptedBytes.size)

        return Base64.encodeToString(ivAndEncryptedData, Base64.DEFAULT)
    }

    // Decrypt data with IV extracted from the ciphertext
    fun decrypt(data: String, secretKey: SecretKey): String {
        val ivAndEncryptedData = Base64.decode(data, Base64.DEFAULT)

        // Extract the IV from the encrypted data
        val iv = ivAndEncryptedData.copyOfRange(0, IV_SIZE)
        val encryptedBytes = ivAndEncryptedData.copyOfRange(IV_SIZE, ivAndEncryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes)
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

    fun encryptMessage(message: Message, secretKey: SecretKey): String {
        val messageJson = gson.toJson(message)
        return encrypt(messageJson, secretKey)
    }

    fun decryptMessage(encryptedMessage: String, secretKey: SecretKey): Message {
        val decryptedJson = decrypt(encryptedMessage, secretKey)
        return gson.fromJson(decryptedJson, Message::class.java)
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