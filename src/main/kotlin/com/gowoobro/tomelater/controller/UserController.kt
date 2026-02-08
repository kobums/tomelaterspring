package com.gowoobro.tomelater.controller

import com.gowoobro.tomelater.entity.User
import com.gowoobro.tomelater.entity.UserCreateRequest
import com.gowoobro.tomelater.entity.UserUpdateRequest
import com.gowoobro.tomelater.entity.UserPatchRequest
import com.gowoobro.tomelater.service.UserService
import com.gowoobro.tomelater.entity.UserResponse

import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService) {

    private fun toResponse(user: User): UserResponse {
        return UserResponse.from(user)
    }

    private fun filterByDateRange(
        value: LocalDateTime?,
        startRange: LocalDateTime?,
        endRange: LocalDateTime?
    ): Boolean {
        if (value == null) return false
        return when {
            startRange != null && endRange != null -> value in startRange..endRange
            startRange != null -> value >= startRange
            endRange != null -> value <= endRange
            else -> true
        }
    }

    @GetMapping
    fun getUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pagesize: Int,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) passwd: String?,
        @RequestParam(required = false) nickname: String?,
        @RequestParam(required = false) socialtype: String?,
        @RequestParam(required = false) socialid: String?,
        @RequestParam(required = false) startcreatedat: LocalDateTime?,
        @RequestParam(required = false) endcreatedat: LocalDateTime?,
        @RequestParam(required = false) startupdatedat: LocalDateTime?,
        @RequestParam(required = false) endupdatedat: LocalDateTime?,
    ): ResponseEntity<Map<String, Any>> {
        var results = if (email != null || passwd != null || nickname != null || socialtype != null || socialid != null || startcreatedat != null || endcreatedat != null || startupdatedat != null || endupdatedat != null || false) {
            var filtered = userService.findAll(0, Int.MAX_VALUE).content
            if (email != null) {
                filtered = filtered.filter { it.email == email }
            }
            if (passwd != null) {
                filtered = filtered.filter { it.passwd == passwd }
            }
            if (nickname != null) {
                filtered = filtered.filter { it.nickname == nickname }
            }
            if (socialtype != null) {
                filtered = filtered.filter { it.socialtype == socialtype }
            }
            if (socialid != null) {
                filtered = filtered.filter { it.socialid == socialid }
            }
            if (startcreatedat != null || endcreatedat != null) {
                filtered = filtered.filter { filterByDateRange(it.createdat, startcreatedat, endcreatedat) }
            }
            if (startupdatedat != null || endupdatedat != null) {
                filtered = filtered.filter { filterByDateRange(it.updatedat, startupdatedat, endupdatedat) }
            }
            filtered
        } else {
            userService.findAll(0, Int.MAX_VALUE).content
        }

        val totalElements = results.size
        val totalPages = if (pagesize > 0) (totalElements + pagesize - 1) / pagesize else 1
        val startIndex = page * pagesize
        val endIndex = minOf(startIndex + pagesize, totalElements)

        val pagedResults = if (startIndex < totalElements) {
            results.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        val response = mapOf(
            "content" to pagedResults.map { toResponse(it) },
            "page" to page,
            "size" to pagesize,
            "totalElements" to totalElements,
            "totalPages" to totalPages,
            "first" to (page == 0),
            "last" to (page >= totalPages - 1),
            "empty" to pagedResults.isEmpty()
        )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val res = userService.findById(id)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }


    @GetMapping("/search/email")
    fun getUserByEmail(@RequestParam email: String): ResponseEntity<List<UserResponse>> {
        val res = userService.findByEmail(email)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/passwd")
    fun getUserByPasswd(@RequestParam passwd: String): ResponseEntity<List<UserResponse>> {
        val res = userService.findByPasswd(passwd)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/nickname")
    fun getUserByNickname(@RequestParam nickname: String): ResponseEntity<List<UserResponse>> {
        val res = userService.findByNickname(nickname)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/socialtype")
    fun getUserBySocialtype(@RequestParam socialtype: String): ResponseEntity<List<UserResponse>> {
        val res = userService.findBySocialtype(socialtype)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/socialid")
    fun getUserBySocialid(@RequestParam socialid: String): ResponseEntity<List<UserResponse>> {
        val res = userService.findBySocialid(socialid)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/createdat")
    fun getUserByCreatedat(@RequestParam createdat: LocalDateTime): ResponseEntity<List<UserResponse>> {
        val res = userService.findByCreatedat(createdat)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/updatedat")
    fun getUserByUpdatedat(@RequestParam updatedat: LocalDateTime): ResponseEntity<List<UserResponse>> {
        val res = userService.findByUpdatedat(updatedat)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }


    @GetMapping("/count")
    fun getCount(): ResponseEntity<Map<String, Long>> {
        val count = userService.count()
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PostMapping
    fun createUser(@RequestBody request: UserCreateRequest): ResponseEntity<Any> {
        return try {
            val res = userService.create(request)
            ResponseEntity.ok(toResponse(res))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(409).body(mapOf("error" to (e.message ?: "요청이 잘못되었습니다.")))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to (e.message ?: "회원가입 처리에 실패했습니다.")))
        }
    }


    @PostMapping("/batch")
    fun createUsers(@RequestBody requests: List<UserCreateRequest>): ResponseEntity<List<UserResponse>> {
        return try {
            val res = userService.createBatch(requests)
            return ResponseEntity.ok(res.map { toResponse(it) } )
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @RequestBody request: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val updatedRequest = request.copy(id = id)
        val res = userService.update(updatedRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}")
    fun patchUser(
        @PathVariable id: Long,
        @RequestBody request: UserPatchRequest
    ): ResponseEntity<UserResponse> {
        val patchRequest = request.copy(id = id)
        val res = userService.patch(patchRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Map<String, Boolean>> {
        val success = userService.deleteById(id)
        return ResponseEntity.ok(mapOf("success" to success))
    }

    @DeleteMapping("/batch")
    fun deleteUsers(@RequestBody entities: List<User>): ResponseEntity<Map<String, Boolean>> {
        val success = userService.deleteBatch(entities)
        return ResponseEntity.ok(mapOf("success" to success))
    }
}