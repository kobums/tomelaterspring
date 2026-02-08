package com.gowoobro.tomelater.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gowoobro.tomelater.repository.QuestionRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.core.io.ResourceLoader
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.LocalDateTime

@Component
class QuestionDataInitializer(
    private val questionRepository: QuestionRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val resourceLoader: ResourceLoader
) : CommandLineRunner {

    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(QuestionDataInitializer::class.java)

    @Transactional
    override fun run(vararg args: String) {
        val count = questionRepository.count()
        logger.debug("Current question count: $count")
        
        if (count == 0L) {
            logger.info("Question table is empty. Initializing with seed data using Batch Insert...")
            try {
                val resource = resourceLoader.getResource("classpath:questions.json")
                if (!resource.exists()) {
                    logger.error("questions.json not found in classpath")
                    return
                }
                
                val questionsMap: List<Map<String, Any>> = objectMapper.readValue(resource.inputStream)
                val sql = "INSERT INTO question_tb (q_content, q_month, q_day, q_createdat, q_updatedat) VALUES (?, ?, ?, ?, ?)"
                
                val now = Timestamp.valueOf(LocalDateTime.now())
                
                jdbcTemplate.batchUpdate(sql, questionsMap, 100) { ps, map ->
                    ps.setString(1, map["content"] as String)
                    ps.setInt(2, map["month"] as Int)
                    ps.setInt(3, map["day"] as Int)
                    ps.setTimestamp(4, now)
                    ps.setTimestamp(5, now)
                }
                
                logger.info("Successfully initialized ${questionsMap.size} questions at once.")
            } catch (e: Exception) {
                logger.error("CRITICAL: Failed to initialize question data. Reason: ${e.message}", e)
            }
        } else {
            logger.info("Question table already contains $count questions. Skipping initial load.")
        }
    }
}
