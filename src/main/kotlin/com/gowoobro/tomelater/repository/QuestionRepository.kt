package com.gowoobro.tomelater.repository

import com.gowoobro.tomelater.entity.Question
import com.gowoobro.tomelater.entity.QuestionCreateRequest
import com.gowoobro.tomelater.entity.QuestionUpdateRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime




@Repository
interface QuestionRepository : JpaRepository<Question, Long> {
    override fun findAll(pageable: Pageable): Page<Question>

    override fun findById(id: Long): java.util.Optional<Question>

    fun findByContent(content: String): List<Question>

    fun findByMonth(month: Int): List<Question>

    fun findByDay(day: Int): List<Question>

    fun findByCreatedat(createdat: LocalDateTime): List<Question>

    fun findByUpdatedat(updatedat: LocalDateTime): List<Question>
}
