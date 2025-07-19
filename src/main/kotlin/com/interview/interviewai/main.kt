package com.interview.interviewai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
//    val mdDefinition = markdownBookDefinition()
//
//    llm.writeSession {
//        val stream = requestLLMStreaming(mdDefinition)
//        // Access the raw string chunks directly
//        stream.collect { chunk ->
//            // Process each chunk of text as it arrives
//            println("Received chunk: $chunk") // The chunks together will be structured as a text following the mdDefinition schema
//        }
//    }
    }