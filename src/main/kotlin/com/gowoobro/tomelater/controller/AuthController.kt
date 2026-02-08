package com.gowoobro.tomelater.controller

import com.gowoobro.tomelater.config.JwtUtil
import com.gowoobro.tomelater.entity.*
import com.gowoobro.tomelater.service.EmailVerificationService
import com.gowoobro.tomelater.service.UserService
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
    
    @PostMapping("/auth/verify/send")
    fun sendVerificationCode(@RequestBody request: VerificationRequest): ResponseEntity<Map<String, String>> {
        return try {
            println("Verification code request for email: ${request.email}")
            emailVerificationService.sendCode(request.email)
            ResponseEntity.ok(mapOf("message" to "인증 코드가 발송되었습니다."))
        } catch (e: Exception) {
            println("Failed to send verification code: ${e.message}")
            e.printStackTrace()
            ResponseEntity.status(500).body(mapOf("error" to "인증 코드 발송에 실패했습니다: ${e.message}"))
        }
    }


    @PostMapping("/auth/verify/check")
    fun checkVerificationCode(@RequestBody request: VerificationRequest): ResponseEntity<Map<String, Boolean>> {
        val isValid = emailVerificationService.verifyCode(request.email, request.code ?: "")
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
        val isValid = emailVerificationService.verifyCode(request.email, request.code)
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


    
    @GetMapping("/jwt")
    fun authenticateUser(
        @RequestParam email: String,
        @RequestParam passwd: String
    ): ResponseEntity<JwtResponse> {
        return try {
            println("JWT Login attempt: email=$email")
            val users = userService.findByEmail(email)
            val user = users.firstOrNull()
            println("User found: ${user != null}")

            if (user != null) {
                println("User password from DB: ${user.passwd}")
                println("Input password: $passwd")
                val passwordMatches = passwordEncoder.matches(passwd, user.passwd)
                println("Password matches: $passwordMatches")

                if (passwordMatches) {
                    val jwt = jwtUtil.generateToken(user.email)
                    println("JWT generated successfully")
                    val userResponse = UserResponse.from(user)
                    ResponseEntity.ok(JwtResponse(jwt, "Bearer", userResponse))
                } else {
                    ResponseEntity.status(401).body(null)
                }
            } else {
                ResponseEntity.status(404).body(null)
            }
        } catch (e: Exception) {
            println("Authentication error: ${e.message}")
            e.printStackTrace()
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

    @GetMapping("/test/encode-password")
    fun encodePassword(@RequestParam password: String): ResponseEntity<Map<String, String?>> {
        val encoded = passwordEncoder.encode(password)
        return ResponseEntity.ok(mapOf("password" to password, "encoded" to encoded))
    }
}
