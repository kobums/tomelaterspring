package com.gowoobro.tomelater.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.security.SecureRandom

@Service
class EmailVerificationService(private val emailService: EmailService) {

    // Key: Email, Value: Pair(Code, ExpirationTime)
    private val verificationCodes = ConcurrentHashMap<String, Pair<String, Long>>()
    
    // 5 minutes expiration
    private val EXPIRATION_MS = 5 * 60 * 1000L

    // 6자리 코드 무차별 대입 방지: 실패 횟수가 이 값에 도달하면 코드를 폐기한다.
    private val MAX_FAILED_ATTEMPTS = 5
    private val failedAttempts = ConcurrentHashMap<String, Int>()
    private val secureRandom = SecureRandom()

    fun sendCode(email: String) {
        val code = (100000..999999).random().toString()
        val expiration = System.currentTimeMillis() + EXPIRATION_MS
        verificationCodes[email] = code to expiration
        failedAttempts.remove(email)
        
        emailService.sendVerificationCode(email, code)
    }

    fun sendResetCode(email: String) {
        val code = (100000..999999).random().toString()
        val expiration = System.currentTimeMillis() + EXPIRATION_MS
        verificationCodes[email] = code to expiration
        failedAttempts.remove(email)
        
        emailService.sendPasswordResetCode(email, code)
    }


    fun verifyCode(email: String, code: String): Boolean {
        val stored = verificationCodes[email] ?: return false
        val (storedCode, expiration) = stored
        
        if (System.currentTimeMillis() > expiration) {
            verificationCodes.remove(email)
            return false
        }
        
        val isValid = storedCode == code
        if (!isValid) {
            val attempts = failedAttempts.merge(email, 1, Int::plus) ?: 1
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                verificationCodes.remove(email)
                failedAttempts.remove(email)
            }
        }
        return isValid
    }

    // 회원가입용: 코드 확인(/api/auth/verify/check)에 성공한 이메일을 일정 시간 "인증됨"으로 기억해 두고,
    // POST /api/user 가 이 표시를 확인·소모한다. 예전엔 isVerified() 가 항상 true 라
    // 인증 단계를 건너뛰고 아무 이메일로나 가입할 수 있었다.
    private val VERIFIED_TTL_MS = 30 * 60 * 1000L
    private val verifiedEmails = ConcurrentHashMap<String, Long>()

    /** 코드를 확인하고, 맞으면 재사용하지 못하게 폐기한다. */
    fun consumeCode(email: String, code: String): Boolean {
        if (!verifyCode(email, code)) return false
        verificationCodes.remove(email)
        failedAttempts.remove(email)
        return true
    }

    fun markVerified(email: String) {
        verifiedEmails[email] = System.currentTimeMillis() + VERIFIED_TTL_MS
    }

    fun isVerified(email: String): Boolean {
        val expiration = verifiedEmails[email] ?: return false
        if (System.currentTimeMillis() > expiration) {
            verifiedEmails.remove(email)
            return false
        }
        return true
    }

    fun clearVerified(email: String) {
        verifiedEmails.remove(email)
    }

    private fun IntRange.random() = start + secureRandom.nextInt(endInclusive - start + 1)
}
