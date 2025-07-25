package websocket

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val interviewWebSocketHandler: InterviewWebSocketHandler
) : WebSocketConfigurer {

    init {
        println(" WebSocketConfig initialized")
    }

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(interviewWebSocketHandler, "/interview")
            .setAllowedOrigins("*") // allow frontend origin here
    }
}