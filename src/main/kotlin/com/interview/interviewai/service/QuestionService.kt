package com.interview.interviewai.service

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.interview.interviewai.config.AppConfig
import com.interview.interviewai.formatOutput
import com.interview.interviewai.model.Question
import com.interview.interviewai.prompt.QUESTION_GENERATION_PROMPT
import org.springframework.stereotype.Service

@Service
class QuestionService(
    private val interviewService: InterviewService,
    private val appConfig: AppConfig
) {


    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    suspend fun generateQuestion(): Question? {
        println("generateQuestion")

        val agent = AIAgent(
            executor = simpleOpenAIExecutor(appConfig.openApiKey),
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

    suspend fun generateQuestionStreaming(onChunk: suspend (String) -> Unit) {
        println("starting question")

        val agentStrategy = strategy("generate-question") {
            val getMdOutput by node<String, String> { input ->
                val books = mutableListOf<String>()

                llm.writeSession {
                    val buffer = mutableListOf<String>()

                    val stream = requestLLMStreaming()
                    stream.collect { chunk ->
                        books.add(chunk)
                        buffer.add(chunk)

                        if (buffer.size == 10) {
                            val combined = buffer.joinToString("")
                            onChunk(combined)
                            buffer.clear()
                        }
                        print(chunk)
                    }
                    if (buffer.isNotEmpty()) {
                        val combined = buffer.joinToString("")
                        onChunk(combined)
                    }
                }

                formatOutput(books)
            }

            edge(nodeStart forwardTo getMdOutput)
            edge(getMdOutput forwardTo nodeFinish)
        }



        val agent = AIAgent(
            executor = simpleOpenAIExecutor(appConfig.openApiKey),
            systemPrompt = QUESTION_GENERATION_PROMPT,
            llmModel = OpenAIModels.Reasoning.GPT4oMini,
            strategy = agentStrategy
        )

        agent.runAndGetResult("Generate question according to the prompt")
    }
}
