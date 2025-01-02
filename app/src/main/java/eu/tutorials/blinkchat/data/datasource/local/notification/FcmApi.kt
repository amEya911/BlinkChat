package eu.tutorials.blinkchat.data.datasource.local.notification

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FcmApi {
    @POST("v1/projects/blinkchat-a44b2/messages:send")
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Path("projectId") projectId: String,
        @Body message: Map<String, Any>
    )

    @POST("/broadcast")
    suspend fun broadcast(@Body body: SendMessageDto)
}

data class SendMessageDto(
    val to: String?,
    val notification: NotificationBody
)

data class NotificationBody(
    val notification: Notification,
    val data: Map<String, String>? = null
) {
    data class Notification(
        val title: String,
        val body: String
    )
}
