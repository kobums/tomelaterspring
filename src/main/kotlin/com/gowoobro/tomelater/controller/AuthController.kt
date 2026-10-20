package com.gowoobro.tomelater.controller

import com.gowoobro.tomelater.config.JwtUtil
import com.gowoobro.tomelater.entity.*
import com.gowoobro.tomelater.service.EmailVerificationService
import com.gowoobro.tomelater.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

data class LoginRequest(val email: String, val passwd: String)
data class VerificationRequest(val email: String, val code: String? = null)
data class FindEmailRequest(val nickname: String)
data class ResetPasswordRequest(val email: String, val code: String, val newPasswd: String)

data class JwtResponse(
    val token: String,
    val type: String = "Bearer",
    val user: UserResponse
)
@RestController
@RequestMapping("/api")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userService: UserService,
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: PasswordEncoder,
    private val emailVerificationService: EmailVerificationService
) {
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    @PostMapping("/auth/verify/send")
    fun sendVerificationCode(@RequestBody request: VerificationRequest): ResponseEntity<Map<String, String>> {
        return try {
            emailVerificationService.sendCode(request.email)
            ResponseEntity.ok(mapOf("message" to "인증 코드가 발송되었습니다."))
        } catch (e: Exception) {
            logger.warn("Failed to send verification code", e)
            ResponseEntity.status(500).body(mapOf("error" to "인증 코드 발송에 실패했습니다."))
        }
    }


    @PostMapping("/auth/verify/check")
    fun checkVerificationCode(@RequestBody request: VerificationRequest): ResponseEntity<Map<String, Boolean>> {
        // 성공하면 코드를 소모하고 이메일을 "인증됨"으로 표시한다(POST /api/user 가 확인).
        val isValid = emailVerificationService.consumeCode(request.email, request.code ?: "")
        if (isValid) {
            emailVerificationService.markVerified(request.email)
        }
        return ResponseEntity.ok(mapOf("valid" to isValid))
    }

    @PostMapping("/auth/find/email")
    fun findEmail(@RequestBody request: FindEmailRequest): ResponseEntity<Map<String, String?>> {
        val users = userService.findByNickname(request.nickname)
        if (users.isEmpty()) {
            return ResponseEntity.status(404).body(mapOf("error" to "해당 닉네임의 사용자를 찾을 수 없습니다."))
        }
        
        // Mask email: exam***@domain.com
        val email = users.first().email
        val atIndex = email.indexOf("@")
        val maskedEmail = if (atIndex > 3) {
            email.substring(0, 3) + "***" + email.substring(atIndex)
        } else {
            email // Too short to mask safely without hiding everything
        }
        
        return ResponseEntity.ok(mapOf("email" to maskedEmail))
    }

    @PostMapping("/auth/password/code")
    fun sendPasswordResetCode(@RequestBody request: VerificationRequest): ResponseEntity<Map<String, String>> {
        if (!userService.findByEmail(request.email).isNotEmpty()) {
             return ResponseEntity.status(404).body(mapOf("error" to "해당 이메일의 계정이 존재하지 않습니다."))
        }
        
        // Reuse verification service logic but calling newly added email service method manually?
        // Or just let verify/send do it? But we want specific email template.
        // Let's manually generate code and send email here, or add method to EmailVerificationService.
        // For simplicity and reusing concurrent map logic, let's reuse EmailVerificationService but modify it to accept type or just use sendCode and send email manually? 
        // Actually, EmailVerificationService.sendCode calls emailService.sendVerificationCode.
        // Let's add sendPasswordResetCode to EmailVerificationService.
        
        // Wait, I can't modify EmailVerificationService in this step easily alongside AuthController in one go if I want to be clean.
        // But I can implement it here temporarily or just use the existing sendCode and existing generic email if I wanted to save time.
        // But user wants "Email, Password Find". 
        // Let's stick to modifying EmailVerificationService in next step or now if possible.
        // I will assume I can modify EmailVerificationService later.
        // For now, let's assume we call a new method `sendResetCode` in EmailVerificationService.
        
        return try {
             // I'll add sendResetCode in next tool call to EmailVerificationService
             // For now let's comment out or use generic sendCode
             // emailVerificationService.sendResetCode(request.email)
             
             // Actually, I should update EmailVerificationService first or just do it in parallel.
             // I will use `emailVerificationService.sendCode(request.email)` but that sends "Registration Code".
             // I need to change EmailVerificationService to support separate templates.
             // Let's skip implementing this endpoint fully until I update EmailVerificationService.
             
             // PLAN CHANGE: I will update EmailVerificationService in this turn as well.
             emailVerificationService.sendResetCode(request.email)
             ResponseEntity.ok(mapOf("message" to "비밀번호 재설정 코드가 발송되었습니다."))
        } catch (e: Exception) {
             ResponseEntity.status(500).body(mapOf("error" to "코드 발송 실패"))
        }
    }
    
    @PostMapping("/auth/password/reset")
    fun resetPassword(@RequestBody request: ResetPasswordRequest): ResponseEntity<Map<String, String>> {
        // 코드는 한 번만 쓸 수 있다(재설정 후 같은 코드로 다시 바꾸지 못하게).
        val isValid = request.newPasswd.isNotBlank() &&
            emailVerificationService.consumeCode(request.email, request.code)
        if (!isValid) {
            return ResponseEntity.status(400).body(mapOf("error" to "인증 코드가 올바르지 않거나 만료되었습니다."))
        }
        
        val success = userService.updatePasswordByEmail(request.email, request.newPasswd)
        return if (success) {
            ResponseEntity.ok(mapOf("message" to "비밀번호가 성공적으로 변경되었습니다."))
        } else {
            ResponseEntity.status(500).body(mapOf("error" to "비밀번호 변경에 실패했습니다."))
        }
    }


    
    /**
     * @deprecated 비밀번호가 쿼리스트링(URL)에 실려 프록시·액세스 로그·브라우저 기록에 남는다.
     * 새 클라이언트는 POST /api/auth/login 을 사용한다. 이미 배포된 앱 버전 호환을 위해서만 남겨 두며,
     * 구버전 앱이 충분히 사라지면 제거한다. 이 핸들러는 요청 값(이메일/비밀번호)을 로그에 남기지 않는다.
     */
    @Deprecated("Use POST /api/auth/login")
    @GetMapping("/jwt")
    fun authenticateUser(
        @RequestParam email: String,
        @RequestParam passwd: String
    ): ResponseEntity<JwtResponse> {
        return try {
            val user = userService.findByEmail(email).firstOrNull()
            // 존재하지 않는 계정과 비밀번호 불일치를 구분하지 않는다(계정 존재 여부 노출 방지).
            if (user != null && passwordEncoder.matches(passwd, user.passwd)) {
                val jwt = jwtUtil.generateToken(user.email)
                ResponseEntity.ok(JwtResponse(jwt, "Bearer", UserResponse.from(user)))
            } else {
                ResponseEntity.status(401).body(null)
            }
        } catch (e: Exception) {
            ResponseEntity.status(500).body(null)
        }
    }
    @PostMapping("/auth/login")
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<JwtResponse> {
        return try {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    loginRequest.email,
                    loginRequest.passwd
                )
            )
            val users = userService.findByEmail(loginRequest.email)
            val user = users.firstOrNull()

            if (user != null) {
                val jwt = jwtUtil.generateToken(loginRequest.email)
                val userResponse = UserResponse.from(user)
                ResponseEntity.ok(JwtResponse(jwt, "Bearer", userResponse))
            } else {
                ResponseEntity.status(404).body(null)
            }
        } catch (e: Exception) {
            ResponseEntity.status(401).body(null)
        }
    }
}
