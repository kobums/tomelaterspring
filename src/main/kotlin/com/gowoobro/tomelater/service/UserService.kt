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
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailVerificationService: EmailVerificationService,
) {

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
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }
        if (userRepository.existsByNickname(request.nickname)) {
            throw IllegalArgumentException("이미 존재하는 닉네임입니다.")
        }
        // 서버에서 이메일 인증을 강제한다: /api/auth/verify/check 를 통과한 이메일만 가입할 수 있다.
        if (!emailVerificationService.isVerified(request.email)) {
            throw IllegalStateException("이메일 인증을 완료해주세요.")
        }

        val now = LocalDateTime.now()
        val entity = User(
            email = request.email,
            passwd = passwordEncoder.encode(request.passwd ?: "")!!,
            nickname = request.nickname,
            socialtype = request.socialtype,
            socialid = request.socialid,
            createdat = now,
            updatedat = now,
        )
        val saved = userRepository.save(entity)
        emailVerificationService.clearVerified(request.email)
        return saved
    }


    fun createBatch(requests: List<UserCreateRequest>): List<User> {
        // 단건 가입과 같은 검사(중복·이메일 인증)를 거친다.
        return requests.map { create(it) }
    }

    fun update(request: UserUpdateRequest): User? {
        val existing = userRepository.findById(request.id).orElse(null) ?: return null
        val requestPasswd = request.passwd ?: ""
        // 응답에서 passwd 를 비워 보내므로, 빈 값이 오면 기존 비밀번호를 유지한다.
        val encodedPassword = if (requestPasswd.isBlank()) {
            existing.passwd
        } else if (requestPasswd != existing.passwd && !requestPasswd.startsWith("\$2a\$")) {
            passwordEncoder.encode(requestPasswd)!!
        } else {
            requestPasswd
        }
        

        val updated = existing.copy(
            // 이메일은 인증을 거쳐야만 바꿀 수 있으므로 일반 수정으로는 변경하지 않는다.
            email = existing.email,
            passwd = encodedPassword,
            nickname = request.nickname,
            socialtype = request.socialtype,
            socialid = request.socialid,
            updatedat = LocalDateTime.now(),
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
            email = existing.email, // 이메일 변경은 인증 없이 허용하지 않는다.
            passwd = request.passwd?.takeIf { it.isNotBlank() }?.let { passwordEncoder.encode(it)!! } ?: existing.passwd,
            nickname = request.nickname ?: existing.nickname,
            socialtype = request.socialtype ?: existing.socialtype,
            socialid = request.socialid ?: existing.socialid,
            updatedat = LocalDateTime.now(),
        )
        return userRepository.save(updated)
    }

    fun updatePasswordByEmail(email: String, newPasswd: String): Boolean {
        val users = userRepository.findByEmail(email)
        val user = users.firstOrNull() ?: return false
        
        val encodedPassword = passwordEncoder.encode(newPasswd)!!
        
        val updatedUser = user.copy(
            passwd = encodedPassword,
            updatedat = LocalDateTime.now()
        )
        userRepository.save(updatedUser)
        return true
    }
}