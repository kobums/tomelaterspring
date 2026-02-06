package com.gowoobro.tomelater.repository

import com.gowoobro.tomelater.entity.Answer
import com.gowoobro.tomelater.entity.AnswerCreateRequest
import com.gowoobro.tomelater.entity.AnswerUpdateRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

import com.gowoobro.tomelater.enums.answer.Ispublic



@Repository
interface AnswerRepository : JpaRepository<Answer, Long> {
    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    override fun findAll(pageable: Pageable): Page<Answer>

    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    override fun findById(id: Long): java.util.Optional<Answer>

    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    fun findByuidId(user: Long): List<Answer>

    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    fun findByqidId(question: Long): List<Answer>

    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    fun findByContent(content: String): List<Answer>

    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    fun findByIspublic(ispublic: Ispublic): List<Answer>

    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    fun findByCreatedat(createdat: LocalDateTime): List<Answer>

    @EntityGraph(attributePaths = [
        "user",
        "question"
    ])
    fun findByUpdatedat(updatedat: LocalDateTime): List<Answer>
}
