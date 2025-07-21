package com.interview.interviewai.controller

import com.interview.interviewai.model.MessageRequest
import com.interview.interviewai.model.MessageResponse
import com.interview.interviewai.service.InterviewService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("interview")
@CrossOrigin(origins = ["http://localhost:8080"]) // allow frontend URL

class InterviewController(
    private val interviewService: InterviewService
) {

    @PostMapping("/message")
    suspend fun handleStudentMessage(@RequestBody request: MessageRequest): MessageResponse {
        return interviewService.processStudentMessage(request)
    }

    @GetMapping("/response")
    fun getLatestResponse(@RequestParam sessionId: String): MessageResponse? {
        return interviewService.getLatestResponse(sessionId)
    }

    @PutMapping("/question")
    fun updateQuestion(
        @RequestParam sessionId: String,
        @RequestBody question: String
    ): String {
        interviewService.updateCurrentQuestion(sessionId, question)
        return "Current question updated successfully for session: $sessionId"
    }
}
