package com.gowoobro.tomelater.service

import com.gowoobro.tomelater.entity.Answer
import com.gowoobro.tomelater.entity.AnswerCreateRequest
import com.gowoobro.tomelater.entity.AnswerUpdateRequest
import com.gowoobro.tomelater.entity.AnswerPatchRequest
import com.gowoobro.tomelater.repository.AnswerRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

import com.gowoobro.tomelater.enums.answer.Ispublic


@Service
@Transactional
class AnswerService(private val answerRepository: AnswerRepository) {

    fun findAll(page: Int = 0, pagesize: Int = 10): Page<Answer> {
        val pageable: Pageable = PageRequest.of(page, pagesize)
        return answerRepository.findAll(pageable)
    }

    fun findById(id: Long): Answer? {
        return answerRepository.findById(id).orElse(null)
    }

    fun count(): Long {
        return answerRepository.count()
    }


    fun findByUid(user: Long): List<Answer> {
        return answerRepository.findByuidId(user)
    }

    fun findByQid(question: Long): List<Answer> {
        return answerRepository.findByqidId(question)
    }

    fun findByContent(content: String): List<Answer> {
        return answerRepository.findByContent(content)
    }

    fun findByIspublic(ispublic: Ispublic): List<Answer> {
        return answerRepository.findByIspublic(ispublic)
    }

    fun findByCreatedat(createdat: LocalDateTime): List<Answer> {
        return answerRepository.findByCreatedat(createdat)
    }

    fun findByUpdatedat(updatedat: LocalDateTime): List<Answer> {
        return answerRepository.findByUpdatedat(updatedat)
    }


    fun create(request: AnswerCreateRequest): Answer {
        val entity = Answer(
            uidId = request.uid,
            qidId = request.qid,
            content = request.content,
            ispublic = request.ispublic,
            createdat = request.createdat,
            updatedat = request.updatedat,
        )
        return answerRepository.save(entity)
    }

    fun createBatch(requests: List<AnswerCreateRequest>): List<Answer> {
        val entities = requests.map { request ->
            Answer(
                uidId = request.uid,
                qidId = request.qid,
                content = request.content,
                ispublic = request.ispublic,
                createdat = request.createdat,
                updatedat = request.updatedat,
            )
        }
        return answerRepository.saveAll(entities)
    }

    fun update(request: AnswerUpdateRequest): Answer? {
        val existing = answerRepository.findById(request.id).orElse(null) ?: return null

        val updated = existing.copy(
            uidId = request.uid,
            qidId = request.qid,
            content = request.content,
            ispublic = request.ispublic,
            createdat = request.createdat,
            updatedat = request.updatedat,
        )
        return answerRepository.save(updated)
    }

    fun delete(entity: Answer): Boolean {
        return try {
            answerRepository.delete(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteById(id: Long): Boolean {
        return try {
            answerRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteBatch(entities: List<Answer>): Boolean {
        return try {
            answerRepository.deleteAll(entities)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun patch(request: AnswerPatchRequest): Answer? {
        val existing = answerRepository.findById(request.id).orElse(null) ?: return null

        val updated = existing.copy(
            uidId = request.uid ?: existing.uidId,
            qidId = request.qid ?: existing.qidId,
            content = request.content ?: existing.content,
            ispublic = request.ispublic ?: existing.ispublic,
            createdat = request.createdat ?: existing.createdat,
            updatedat = request.updatedat ?: existing.updatedat,
        )
        return answerRepository.save(updated)
    }
}