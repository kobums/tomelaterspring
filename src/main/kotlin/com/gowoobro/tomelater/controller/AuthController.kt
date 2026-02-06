package com.gowoobro.tomelater.controller

import com.gowoobro.tomelater.config.JwtUtil
import com.gowoobro.tomelater.entity.*
import com.gowoobro.tomelater.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
data class LoginRequest(val email: String, val passwd: String)
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
    private val passwordEncoder: PasswordEncoder
) {
    
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
