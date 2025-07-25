package com.interview.interviewai.controller

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.ToolRegistry.Companion.invoke
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.structure.markdown.MarkdownStructuredDataDefinition
import com.interview.interviewai.CalculatorTools
import com.interview.interviewai.config.AppConfig
import com.interview.interviewai.prompt.QUESTION_GENERATION_PROMPT
import com.interview.interviewai.formatOutput
import com.interview.interviewai.model.Question
import com.interview.interviewai.service.QuestionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("question")
class QuestionController(
    private val questionService: QuestionService,
    private val appConfig: AppConfig,
) {

    @GetMapping("")
    suspend fun getQuestion(): Question? {
        println("Generating question")
        return questionService.generateQuestion()
    }


    @GetMapping("1")
    suspend fun getQuestionStreaming() : String {
        println("starting question")

        val agentStrategy = strategy("generate-question") {
            val getMdOutput by node<String, String> { input ->
                val books = mutableListOf<String>()
                val mdDefinition = markdownBookDefinition()


                llm.writeSession {
                    val stream = requestLLMStreaming()
                    // Access the raw string chunks directly
                    stream.collect { chunk ->
                        books.add(chunk)
                        // Process each chunk of text as it arrives
                        print(chunk)
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

        val result = agent.runAndGetResult("Generate question according to the prompt")
        return result?:""
    }

    fun markdownBookDefinition(): MarkdownStructuredDataDefinition {
        return MarkdownStructuredDataDefinition("name", schema = { /*...*/ })
    }

    fun d(){
        val agentStrategy = strategy("Simple calculator") {
            val nodeSendInput by nodeLLMRequest()
            val nodeExecuteTool by nodeExecuteTool()
            val nodeSendToolResult by nodeLLMSendToolResult()

            // Define edges between nodes
            // Start -> Send input
            edge(nodeStart forwardTo nodeSendInput)



            edge(
                (nodeSendInput forwardTo nodeFinish)
                        transformed { it }
                        onAssistantMessage { true }
            )

            // Send input -> Execute tool
            edge(
                (nodeSendInput forwardTo nodeExecuteTool)
                        onToolCall { true }
            )

            // Execute tool -> Send the tool result
            edge(nodeExecuteTool forwardTo nodeSendToolResult)

            // Send the tool result -> finish
            edge(
                (nodeSendToolResult forwardTo nodeFinish)
                        transformed { it }
                        onAssistantMessage { true }
            )
        }

    }


    val agentConfig = AIAgentConfig.withSystemPrompt(
        prompt = """
        You are a simple calculator assistant.
        You can add two numbers together using the calculator tool.
        When the user provides input, extract the numbers they want to add.
        The input might be in various formats like "add 5 and 7", "5 + 7", or just "5 7".
        Extract the two numbers and use the calculator tool to add them.
        Always respond with a clear, friendly message showing the calculation and result.
        """.trimIndent()
    )
    val toolRegistry = ToolRegistry {
        tools(CalculatorTools())
    }

    private fun ToolRegistry.Builder.tools(toolsList: CalculatorTools) {}



}



