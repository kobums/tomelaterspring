package com.gowoobro.tomelater.service

import com.gowoobro.tomelater.entity.User
import com.gowoobro.tomelater.entity.UserCreateRequest
import com.gowoobro.tomelater.entity.UserUpdateRequest
import com.gowoobro.tomelater.entity.UserPatchRequest
import com.gowoobro.tomelater.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime



@Service
@Transactional
class UserService(private val userRepository: UserRepository, private val passwordEncoder: PasswordEncoder) {

    fun findAll(page: Int = 0, pagesize: Int = 10): Page<User> {
        val pageable: Pageable = PageRequest.of(page, pagesize)
        return userRepository.findAll(pageable)
    }

    fun findById(id: Long): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun count(): Long {
        return userRepository.count()
    }


    fun findByEmail(email: String): List<User> {
        return userRepository.findByEmail(email)
    }

    fun findByPasswd(passwd: String): List<User> {
        return userRepository.findByPasswd(passwd)
    }

    fun findByNickname(nickname: String): List<User> {
        return userRepository.findByNickname(nickname)
    }

    fun findBySocialtype(socialtype: String): List<User> {
        return userRepository.findBySocialtype(socialtype)
    }

    fun findBySocialid(socialid: String): List<User> {
        return userRepository.findBySocialid(socialid)
    }

    fun findByCreatedat(createdat: LocalDateTime): List<User> {
        return userRepository.findByCreatedat(createdat)
    }

    fun findByUpdatedat(updatedat: LocalDateTime): List<User> {
        return userRepository.findByUpdatedat(updatedat)
    }


    fun create(request: UserCreateRequest): User {
        val entity = User(
            email = request.email,
            passwd = passwordEncoder.encode(request.passwd ?: "")!!,
            nickname = request.nickname,
            socialtype = request.socialtype,
            socialid = request.socialid,
            createdat = request.createdat ?: LocalDateTime.now(),
            updatedat = request.updatedat ?: LocalDateTime.now(),
        )
        return userRepository.save(entity)
    }

    fun createBatch(requests: List<UserCreateRequest>): List<User> {
        val entities = requests.map { request ->
            User(
                email = request.email,
                passwd = passwordEncoder.encode(request.passwd ?: "")!!,
                nickname = request.nickname,
                socialtype = request.socialtype,
                socialid = request.socialid,
                createdat = request.createdat ?: LocalDateTime.now(),
                updatedat = request.updatedat ?: LocalDateTime.now(),
            )
        }
        return userRepository.saveAll(entities)
    }

    fun update(request: UserUpdateRequest): User? {
        val existing = userRepository.findById(request.id).orElse(null) ?: return null
        val requestPasswd = request.passwd ?: ""
        val encodedPassword = if (requestPasswd != existing.passwd && !requestPasswd.startsWith("\$2a\$")) {
            passwordEncoder.encode(requestPasswd)!!
        } else {
            requestPasswd
        }
        

        val updated = existing.copy(
            email = request.email,
            passwd = encodedPassword,
            nickname = request.nickname,
            socialtype = request.socialtype,
            socialid = request.socialid,
            createdat = request.createdat,
            updatedat = request.updatedat,
        )
        return userRepository.save(updated)
    }

    fun delete(entity: User): Boolean {
        return try {
            userRepository.delete(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteById(id: Long): Boolean {
        return try {
            userRepository.deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteBatch(entities: List<User>): Boolean {
        return try {
            userRepository.deleteAll(entities)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun patch(request: UserPatchRequest): User? {
        val existing = userRepository.findById(request.id).orElse(null) ?: return null

        val updated = existing.copy(
            email = request.email ?: existing.email,
            passwd = request.passwd ?: existing.passwd,
            nickname = request.nickname ?: existing.nickname,
            socialtype = request.socialtype ?: existing.socialtype,
            socialid = request.socialid ?: existing.socialid,
            createdat = request.createdat ?: existing.createdat,
            updatedat = request.updatedat ?: existing.updatedat,
        )
        return userRepository.save(updated)
    }
}