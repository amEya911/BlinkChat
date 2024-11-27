package eu.tutorials.blinkchat.util

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.security.MessageDigest

object HashUtil {
    fun hashPhoneNumber(phoneNumber: String): String {
        val phoneNumberUtil = PhoneNumberUtil.getInstance()
        return try {
            val parsedNumber = phoneNumberUtil.parse(phoneNumber, "IN")
            val formattedNumber = phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            val bytes = MessageDigest.getInstance("SHA-256").digest(formattedNumber.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e("HashUtil", "Failed to parse phone number: $phoneNumber", e)
            ""
        }
    }
}
