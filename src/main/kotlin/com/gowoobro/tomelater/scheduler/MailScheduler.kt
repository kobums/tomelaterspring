package com.gowoobro.tomelater.scheduler

import com.gowoobro.tomelater.repository.AnswerRepository
import com.gowoobro.tomelater.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Component
class MailScheduler(
    private val answerRepository: AnswerRepository,
    private val emailService: EmailService
) {

    private val logger = LoggerFactory.getLogger(MailScheduler::class.java)

    // 매일 오전 9시에 실행
    @Scheduled(cron = "0 0 9 * * *")
    fun sendTimeCapsuleEmails() {
        val today = LocalDate.now()
        val oneYearAgo = today.minusYears(1)
        
        // 1년 전 00:00:00 ~ 23:59:59 사이의 답변 조회
        val startOfDay = oneYearAgo.atStartOfDay()
        val endOfDay = oneYearAgo.atTime(LocalTime.MAX)
        
        logger.info("Starting Time Capsule Email Scheduler for answers created between $startOfDay and $endOfDay")

        val answers = answerRepository.findByCreatedatBetween(startOfDay, endOfDay)
        
        if (answers.isEmpty()) {
            logger.info("No answers found from 1 year ago.")
            return
        }

        logger.info("Found ${answers.size} answers to send.")

        var successCount = 0
        var failCount = 0

        answers.forEach { answer ->
            try {
                // User와 Question이 null이 아닌지 확인 (Lazy Loading 이슈 방지 위해 EntityGraph 사용)
                val user = answer.user
                val question = answer.question
                
                if (user != null && question != null) {
                    emailService.sendTimeCapsuleEmail(
                        to = user.email,
                        nickname = user.nickname,
                        question = question.content,
                        answer = answer.content,
                        date = answer.createdat?.toLocalDate()?.toString() ?: oneYearAgo.toString()
                    )
                    successCount++
                    logger.info("Sent email to user: ${user.id} for answer: ${answer.id}")
                } else {
                    logger.warn("Skipping answer ${answer.id}: User or Question not found.")
                    failCount++
                }
            } catch (e: Exception) {
                logger.error("Failed to send email for answer ${answer.id}", e)
                failCount++
            }
        }
        
        logger.info("Time Capsule Email Scheduler finished. Success: $successCount, Failed: $failCount")
    }
}
