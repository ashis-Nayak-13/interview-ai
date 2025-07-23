package com.interview.interviewai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import com.interview.interviewai.config.AppConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("default")
class DefaultController(
    private val appConfig: AppConfig,
) {



    @GetMapping("tool-registration-demo")
    suspend fun getDemo(): String? {


        val agent = AIAgent(
            executor = simpleOpenAIExecutor(appConfig.openApiKey),
            systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
            llmModel = OpenAIModels.CostOptimized.O4Mini,
            temperature = 0.7,
            toolRegistry = ToolRegistry {
                tool(SayToUser)
            },
            maxIterations = 30
        )

        val result = agent.runAndGetResult("Hi How are you?")

        return  result


    }

    @GetMapping("node-edge-demo")
    suspend fun nodeEdgeDemo() : String? {


        val promptExecutor = simpleOpenAIExecutor(appConfig.openApiKey)

        val agentStrategy = strategy("Simple calculator") {
            val nodeSendInput by nodeLLMRequest()
            val nodeExecuteTool by nodeExecuteTool()
            val nodeSendToolResult by nodeLLMSendToolResult()
            edge(nodeStart forwardTo nodeSendInput)
            edge((nodeSendInput forwardTo nodeExecuteTool) onToolCall { true })
            // Execute tool -> Send the tool result
            edge(nodeExecuteTool forwardTo nodeSendToolResult)
            // Send the tool result -> finish
            edge((nodeSendToolResult forwardTo nodeFinish) transformed { it } onAssistantMessage { true })
        }

        val agentConfig = AIAgentConfig(
            prompt = Prompt.build("simple-calculator") {
                system(
                    """
                You are a simple calculator assistant.
                You can add two numbers together using the calculator tool.
                When the user provides input, extract the numbers they want to add.
                The input might be in various formats like "add 5 and 7", "5 + 7", or just "5 7".
                Extract the two numbers and use the calculator tool to add them.
                Always respond with a clear, friendly message showing the calculation and result.
                """.trimIndent()
                )
            },
            model = OpenAIModels.Chat.GPT4o,
            maxAgentIterations = 10

        )

        val toolRegistry = ToolRegistry {
            tools(CalculatorTools().asTools())
        }

        val agent1 = AIAgent(
            promptExecutor = promptExecutor,
            toolRegistry = toolRegistry,
            strategy = agentStrategy,
            agentConfig = agentConfig,
            installFeatures = {
                install(EventHandler) {
                    onBeforeAgentStarted { strategy, agent ->
                        println("Starting strategy: ${strategy.name}")
                    }
                    onAgentFinished { strategy, result ->
                        println("Result: ${result}")
                    }
                }
            }
        )
        println("Enter two numbers to add (e.g., 'add 5 and 7' or '5 + 7'):")
        val userInput =" 25 and 48"
        val agentResult = agent1.runAndGetResult(userInput)
        println("The agent returned: $agentResult")
        return agentResult
    }
}