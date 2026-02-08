package com.gowoobro.tomelater.repository

import com.gowoobro.tomelater.entity.User
import com.gowoobro.tomelater.entity.UserCreateRequest
import com.gowoobro.tomelater.entity.UserUpdateRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime




@Repository
interface UserRepository : JpaRepository<User, Long> {
    override fun findAll(pageable: Pageable): Page<User>

    override fun findById(id: Long): java.util.Optional<User>

    fun findByEmail(email: String): List<User>

    fun findByPasswd(passwd: String): List<User>

    fun findByNickname(nickname: String): List<User>

    fun findBySocialtype(socialtype: String): List<User>

    fun findBySocialid(socialid: String): List<User>

    fun findByCreatedat(createdat: LocalDateTime): List<User>

    fun findByUpdatedat(updatedat: LocalDateTime): List<User>

    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean
}

