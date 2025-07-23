package com.interview.interviewai.config


import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppConfig(
    @Value("\${OPEN_API_KEY}")
    val openApiKey: String,
)
