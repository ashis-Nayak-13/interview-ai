package com.interview.interviewai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("websocket","com.interview.interviewai.*")

class InterviewAiApplication

fun main(args: Array<String>) {
    runApplication<InterviewAiApplication>(*args)
}
