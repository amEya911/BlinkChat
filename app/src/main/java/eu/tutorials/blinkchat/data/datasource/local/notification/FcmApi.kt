package eu.tutorials.blinkchat.data.datasource.local.notification

import retrofit2.http.Body
import retrofit2.http.POST

interface FcmApi {
    @POST("/send")
    suspend fun sendMessage(@Body body: SendMessageDto)

    @POST("/broadcast")
    suspend fun broadcast(@Body body: SendMessageDto)
}

data class SendMessageDto(
    val to: String?, // If `null`, it broadcasts to all
    val notification: NotificationBody
)

data class NotificationBody(
    val title: String,
    val body: String
)
