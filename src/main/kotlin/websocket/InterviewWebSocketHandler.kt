package websocket

import com.interview.interviewai.service.QuestionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class InterviewWebSocketHandler(
    private val questionService: QuestionService
) : TextWebSocketHandler() {

    private val channelSessions: MutableMap<String, MutableCollection<WebSocketSession>> = ConcurrentHashMap()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val channelId = extractChannelId(session) ?: "default"
        println("Connection established: sessionId=${session.id}, channelId=$channelId")

        session.attributes["channelId"] = channelId
        channelSessions.computeIfAbsent(channelId) { ConcurrentLinkedQueue() }.add(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val channelId = session.attributes["channelId"] as? String ?: "default"
        val payload = message.payload
        println("Received message from sessionId=${session.id} | payload=$payload")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                questionService.generateQuestionStreaming { chunk ->
                    if (session.isOpen) {
                        broadcastToChannel(channelId, chunk)
                    }
                }
            } catch (e: Exception) {
                println("Error in streaming question: ${e.message}")
            }
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val channelId = session.attributes["channelId"] as? String ?: "default"
        channelSessions[channelId]?.remove(session)
        println("Session closed: sessionId=${session.id}, channelId=$channelId")

        if (channelSessions[channelId]?.isEmpty() == true) {
            channelSessions.remove(channelId)
            println("🧹 Removed empty channel: $channelId")
        }
    }

    private fun broadcastToChannel(channelId: String, message: String) {
        channelSessions[channelId]?.forEach { session ->
            if (session.isOpen) {
                session.sendMessage(TextMessage(message))
            }
        }
    }

    private fun extractChannelId(session: WebSocketSession): String? {
        val uri = session.uri ?: return null
        return URI(uri.toString()).query
            ?.split("&")
            ?.firstOrNull { it.startsWith("channelId=") }
            ?.substringAfter("=")
    }
}
