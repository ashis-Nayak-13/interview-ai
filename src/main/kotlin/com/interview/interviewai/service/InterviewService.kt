package com.interview.interviewai.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.interview.interviewai.model.InterviewState
import com.interview.interviewai.model.MessageRequest
import com.interview.interviewai.model.MessageResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class InterviewService(
//    @Value("\${openai.api-key}") private val apiKey: String

) {
    private val sessionStates = ConcurrentHashMap<String, InterviewState>()
    private val apiKey = ""


    private fun getOrCreateState(sessionId: String): InterviewState {
        return sessionStates.computeIfAbsent(sessionId) {
            InterviewState(currentQuestion = "Explain how HashMap works in Java.")
        }
    }

    fun updateCurrentQuestion(sessionId: String, newQuestion: String) {
        val state = getOrCreateState(sessionId)
        state.currentQuestion = newQuestion
    }

    fun getCurrentQuestion(sessionId: String): String {
        return getOrCreateState(sessionId).currentQuestion
    }

    suspend fun processStudentMessage(request: MessageRequest): MessageResponse {
        val state = getOrCreateState(request.sessionId)

        state.lastStudentMessage = request.message

        val systemPrompt = """
You are a backend interview assistant helping students think and learn. 

Context:
- Student is answering this question: "${state.currentQuestion}"
- If student is making a mistake, correct them gently but let them think further.
- Give hints when needed.
- Keep the tone encouraging and constructive.
- Your responses must be short, actionable, and sound like a live conversation.
""".trimIndent()

        val agent = AIAgent(
            executor = simpleOpenAIExecutor(apiKey),
            systemPrompt = systemPrompt,
            llmModel = OpenAIModels.Reasoning.GPT4oMini
        )

        val aiReply = agent.runAndGetResult("""
Student said: "${request.message}"
Now respond like a mock interview assistant as per the above guidelines.
""".trimIndent())

        state.lastAIResponse = aiReply

        println("state.currentQuestion: "+state.currentQuestion)
        println("state.studentLastMessage "+state.lastStudentMessage)
        println("state.lastAIResponse "+state.lastAIResponse)

        return MessageResponse(message = aiReply ?: "")
    }

    fun getLatestResponse(sessionId: String): MessageResponse? {
        return sessionStates[sessionId]?.lastAIResponse?.let { MessageResponse(it) }
    }
}
