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
        if (isValid) {
            // Once verified, we could keep it as verified for a short period
            // or just return true and let the caller handle it.
            // For now, we keep it so the registration can check it.
        }
        return isValid
    }
    
    fun isVerified(email: String): Boolean {
        // This could be more sophisticated, but for now we just verify against the stored code
        // in a separate step or just rely on the verifyCode call.
        return true // Simplified or adjusted based on flow
    }

    private fun IntRange.random() = start + secureRandom.nextInt(endInclusive - start + 1)
}
