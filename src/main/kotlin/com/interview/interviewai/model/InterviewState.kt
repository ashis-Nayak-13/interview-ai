package com.interview.interviewai.model

data class InterviewState(
    var currentQuestion: String,
    var lastStudentMessage: String? = null,
    var lastAIResponse: String? = null
)
