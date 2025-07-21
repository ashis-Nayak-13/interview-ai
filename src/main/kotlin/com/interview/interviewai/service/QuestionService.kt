package com.interview.interviewai.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.interview.interviewai.model.Question
import com.interview.interviewai.prompt.QUESTION_GENERATION_PROMPT
import org.springframework.stereotype.Service

@Service
class QuestionService(
    private val interviewService: InterviewService
) {

    private val apiKey = ""

    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    suspend fun generateQuestion(): Question? {
        println("generateQuestion")

        val agent = AIAgent(
            executor = simpleOpenAIExecutor(apiKey),
            systemPrompt = QUESTION_GENERATION_PROMPT,
            llmModel = OpenAIModels.Reasoning.GPT4oMini
        )

        val result = agent.runAndGetResult("Generate question according to the prompt")
        println(result)
        return try {
            val question = objectMapper.readValue(result, Question::class.java)
            interviewService.updateCurrentQuestion(sessionId = "123", newQuestion = question.questionDescription)
            question
        } catch (e: Exception) {
            println("Error parsing response: ${e.message}")
            null
        }
    }
}
