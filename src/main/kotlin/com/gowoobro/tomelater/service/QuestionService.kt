package com.gowoobro.tomelater.service

import com.gowoobro.tomelater.entity.Question
import com.gowoobro.tomelater.entity.QuestionCreateRequest
import com.gowoobro.tomelater.entity.QuestionUpdateRequest
import com.gowoobro.tomelater.entity.QuestionPatchRequest
import com.gowoobro.tomelater.repository.QuestionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime



@Service
@Transactional
class QuestionService(private val questionRepository: QuestionRepository) {

    fun findAll(page: Int = 0, pagesize: Int = 10): Page<Question> {
        val pageable: Pageable = PageRequest.of(page, pagesize)
        return questionRepository.findAll(pageable)
    }

    fun findById(id: Long): Question? {
        return questionRepository.findById(id).orElse(null)
    }

    fun count(): Long {
        return questionRepository.count()
    }


    fun findByContent(content: String): List<Question> {
        return questionRepository.findByContent(content)
    }

    fun findByMonth(month: Int): List<Question> {
        return questionRepository.findByMonth(month)
    }

    fun findByDay(day: Int): List<Question> {
        return questionRepository.findByDay(day)
    }

    fun findByCreatedat(createdat: LocalDateTime): List<Question> {
        return questionRepository.findByCreatedat(createdat)
    }

    fun findByUpdatedat(updatedat: LocalDateTime): List<Question> {
        return questionRepository.findByUpdatedat(updatedat)
    }


    fun create(request: QuestionCreateRequest): Question {
        val now = LocalDateTime.now()
        val entity = Question(
            content = request.content,
            month = request.month,
            day = request.day,
            createdat = now,
            updatedat = now,
        )
        return questionRepository.save(entity)
    }

    fun createBatch(requests: List<QuestionCreateRequest>): List<Question> {
        // Let's check QuestionService.kt line 68. The user's file showed fun createBatch(requests: List<QuestionCreateRequest>): List<Question>
        val now = LocalDateTime.now()
        val entities = requests.map { request ->
            Question(
                content = request.content,
                month = request.month,
                day = request.day,
                createdat = now,
                updatedat = now,
            )
        }
        return questionRepository.saveAll(entities)
    }

    fun update(request: QuestionUpdateRequest): Question? {
        val existing = questionRepository.findById(request.id).orElse(null) ?: return null

        val updated = existing.copy(
            content = request.content,
            month = request.month,
            day = request.day,
            updatedat = LocalDateTime.now(),
        )
        return questionRepository.save(updated)
    }

    fun delete(entity: Question): Boolean {
        return try {
            questionRepository.delete(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteById(id: Long): Boolean {
        return try {
            questionRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteBatch(entities: List<Question>): Boolean {
        return try {
            questionRepository.deleteAll(entities)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun patch(request: QuestionPatchRequest): Question? {
        val existing = questionRepository.findById(request.id).orElse(null) ?: return null

        val updated = existing.copy(
            content = request.content ?: existing.content,
            month = request.month ?: existing.month,
            day = request.day ?: existing.day,
            updatedat = LocalDateTime.now(),
        )
        return questionRepository.save(updated)
    }
}