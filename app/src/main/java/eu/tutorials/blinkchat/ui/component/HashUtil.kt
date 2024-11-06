package eu.tutorials.blinkchat.ui.component

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.security.MessageDigest

object HashUtil {
    // Function to create a unique ID by formatting to E.164 and hashing the phone number
    fun hashPhoneNumber(phoneNumber: String): String {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        return try {
            val parsedNumber = phoneNumberUtil.parse(phoneNumber, "IN") // Use consistent country code
            val formattedNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            val bytes = MessageDigest.getInstance("SHA-256").digest(formattedNumber.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("HashUtil", "Failed to parse phone number: $phoneNumber", e)
            ""
        }
    }
}
