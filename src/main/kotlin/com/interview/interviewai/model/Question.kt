package com.interview.interviewai.model

data class Question(
    val questionId: Int,
    val questionTitle: String,
    val questionDescription: String,
    val constraints: List<String>,
    val example: Example,
    val hints: List<String>,
    val tags: List<String>,
    val difficulty: String
)

data class Example(
    val input: String,
    val output: String
)
