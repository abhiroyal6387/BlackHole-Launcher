package org.blackhole.launcher

import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class LokiMessage(
    val role: String,
    val text: String
)

object LokiClient {

    private const val API_URL =
        "http://127.0.0.1:3000/api/chat"

    fun sendMessage(
        messages: List<LokiMessage>,
        callback: (String?, String?) -> Unit
    ) {
        Thread {
            var connection: HttpURLConnection? = null

            try {
                connection = URL(API_URL)
                    .openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 60000
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=utf-8"
                )

                val messageArray = JSONArray()

                messages.takeLast(12).forEach { message ->
                    val item = JSONObject()
                    item.put("role", message.role)
                    item.put("text", message.text)
                    messageArray.put(item)
                }

                val body = JSONObject()
                body.put("messages", messageArray)

                connection.outputStream.use { output ->
                    output.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val status = connection.responseCode

                val stream = if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                val response = stream.bufferedReader().use {
                    it.readText()
                }

                val json = JSONObject(response)

                if (status in 200..299) {
                    val reply = json.optString("reply")
                    if (reply.isBlank()) {
                        throw Exception("Loki returned an empty reply.")
                    }

                    Handler(Looper.getMainLooper()).post {
                        callback(reply, null)
                    }
                } else {
                    val error = json.optString(
                        "error",
                        "Server error: HTTP $status"
                    )

                    Handler(Looper.getMainLooper()).post {
                        callback(null, error)
                    }
                }

            } catch (e: Exception) {
                val errorMessage =
                    e.message ?: "Connection failed."

                Handler(Looper.getMainLooper()).post {
                    callback(
                        null,
                        "Loki server se connection nahi ho paya: $errorMessage"
                    )
                }

            } finally {
                connection?.disconnect()
            }

        }.start()
    }
}
