package com.gowoobro.tomelater.controller

import com.gowoobro.tomelater.entity.Answer
import com.gowoobro.tomelater.entity.AnswerCreateRequest
import com.gowoobro.tomelater.entity.AnswerUpdateRequest
import com.gowoobro.tomelater.entity.AnswerPatchRequest
import com.gowoobro.tomelater.service.AnswerService
import com.gowoobro.tomelater.service.UserService
import com.gowoobro.tomelater.entity.AnswerResponse
import com.gowoobro.tomelater.enums.answer.Ispublic

import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/answer")
class AnswerController(
    private val answerService: AnswerService,
    private val userService: UserService) {

    // 토큰의 사용자 id (알 수 없으면 0 — 실제 사용자 id 와 겹치지 않는다).
    private fun viewerId(): Long {
        val email = SecurityContextHolder.getContext().authentication?.name ?: return 0L
        return userService.findByEmail(email).firstOrNull()?.id ?: 0L
    }

    // 비공개 답변은 작성자 본인에게만 보인다(목록·검색·단건 조회 공통).
    private fun canView(answer: Answer, viewer: Long): Boolean =
        answer.ispublic == Ispublic.PUBLIC || answer.uidId == viewer

    private fun toResponse(answer: Answer, viewer: Long): AnswerResponse {
        val res = AnswerResponse.from(answer)
        // 다른 사람의 (공개) 답변에 딸린 작성자 정보에서는 이메일·소셜 ID 를 뺀다.
        if (answer.uidId == viewer) return res
        return res.copy(extra = res.extra.copy(user = res.extra.user?.copy(email = "", socialid = "")))
    }

    private fun visible(items: List<Answer>, viewer: Long): List<AnswerResponse> =
        items.filter { canView(it, viewer) }.map { toResponse(it, viewer) }

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
    fun getAnswers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pagesize: Int,
        @RequestParam(required = false) uid: Long?,
        @RequestParam(required = false) qid: Long?,
        @RequestParam(required = false) content: String?,
        @RequestParam(required = false) ispublic: Int?,
        @RequestParam(required = false) startcreatedat: LocalDateTime?,
        @RequestParam(required = false) endcreatedat: LocalDateTime?,
        @RequestParam(required = false) startupdatedat: LocalDateTime?,
        @RequestParam(required = false) endupdatedat: LocalDateTime?,
    ): ResponseEntity<Map<String, Any>> {
        val viewer = viewerId()
        var results = if (uid != null || qid != null || content != null || ispublic != null || startcreatedat != null || endcreatedat != null || startupdatedat != null || endupdatedat != null || false) {
            var filtered = answerService.findAll(0, Int.MAX_VALUE).content
            if (uid != null) {
                filtered = filtered.filter { it.uidId == uid }
            }
            if (qid != null) {
                filtered = filtered.filter { it.qidId == qid }
            }
            if (content != null) {
                filtered = filtered.filter { it.content == content }
            }
            if (ispublic != null) {
                filtered = filtered.filter { it.ispublic.ordinal == ispublic }
            }
            if (startcreatedat != null || endcreatedat != null) {
                filtered = filtered.filter { filterByDateRange(it.createdat, startcreatedat, endcreatedat) }
            }
            if (startupdatedat != null || endupdatedat != null) {
                filtered = filtered.filter { filterByDateRange(it.updatedat, startupdatedat, endupdatedat) }
            }
            filtered
        } else {
            answerService.findAll(0, Int.MAX_VALUE).content
        }

        results = results.filter { canView(it, viewer) }

        // Sort by ID descending (newest first)
        results = results.sortedByDescending { it.id }

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
            "content" to pagedResults.map { toResponse(it, viewer) },
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
    fun getAnswer(@PathVariable id: Long): ResponseEntity<AnswerResponse> {
        val viewer = viewerId()
        val res = answerService.findById(id)?.takeIf { canView(it, viewer) }
        return if (res != null) {
            ResponseEntity.ok(toResponse(res, viewer))
        } else {
            ResponseEntity.notFound().build()
        }
    }


    @GetMapping("/search/uid")
    fun getAnswerByUid(@RequestParam uid: Long): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByUid(uid)
        return ResponseEntity.ok(visible(res, viewerId()))
    }

    @GetMapping("/search/qid")
    fun getAnswerByQid(@RequestParam qid: Long): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByQid(qid)
        return ResponseEntity.ok(visible(res, viewerId()))
    }

    @GetMapping("/search/content")
    fun getAnswerByContent(@RequestParam content: String): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByContent(content)
        return ResponseEntity.ok(visible(res, viewerId()))
    }

    @GetMapping("/search/ispublic")
    fun getAnswerByIspublic(@RequestParam ispublic: Ispublic): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByIspublic(ispublic)
        return ResponseEntity.ok(visible(res, viewerId()))
    }

    @GetMapping("/search/createdat")
    fun getAnswerByCreatedat(@RequestParam createdat: LocalDateTime): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByCreatedat(createdat)
        return ResponseEntity.ok(visible(res, viewerId()))
    }

    @GetMapping("/search/updatedat")
    fun getAnswerByUpdatedat(@RequestParam updatedat: LocalDateTime): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByUpdatedat(updatedat)
        return ResponseEntity.ok(visible(res, viewerId()))
    }


    @GetMapping("/count")
    fun getCount(): ResponseEntity<Map<String, Long>> {
        val count = answerService.count()
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PostMapping
    fun createAnswer(@RequestBody request: AnswerCreateRequest): ResponseEntity<AnswerResponse> {
        val viewer = viewerId()
        if (viewer == 0L) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return try {
            // 작성자는 토큰의 사용자로 고정한다(남의 uid 로 답변을 만들지 못하게).
            val res = answerService.create(request.copy(uid = viewer))
            ResponseEntity.ok(toResponse(res, viewer))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/batch")
    fun createAnswers(@RequestBody requests: List<AnswerCreateRequest>): ResponseEntity<List<AnswerResponse>> {
        val viewer = viewerId()
        if (viewer == 0L) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        return try {
            val res = answerService.createBatch(requests.map { it.copy(uid = viewer) })
            return ResponseEntity.ok(res.map { toResponse(it, viewer) } )
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updateAnswer(
        @PathVariable id: Long,
        @RequestBody request: AnswerUpdateRequest
    ): ResponseEntity<AnswerResponse> {
        val viewer = viewerId()
        val existing = answerService.findById(id) ?: return ResponseEntity.notFound().build()
        if (existing.uidId != viewer) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        // uid 는 바꿀 수 없다(답변 소유권 이전 방지).
        val updatedRequest = request.copy(id = id, uid = existing.uidId)
        val res = answerService.update(updatedRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res, viewer))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}")
    fun patchAnswer(
        @PathVariable id: Long,
        @RequestBody request: AnswerPatchRequest
    ): ResponseEntity<AnswerResponse> {
        val viewer = viewerId()
        val existing = answerService.findById(id) ?: return ResponseEntity.notFound().build()
        if (existing.uidId != viewer) return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        val patchRequest = request.copy(id = id, uid = null) // uid 변경 무시(소유권 이전 방지)
        val res = answerService.patch(patchRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res, viewer))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteAnswer(@PathVariable id: Long): ResponseEntity<Map<String, Boolean>> {
        val existing = answerService.findById(id)
        if (existing == null || existing.uidId != viewerId()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("success" to false))
        }
        val success = answerService.deleteById(id)
        return ResponseEntity.ok(mapOf("success" to success))
    }

    @DeleteMapping("/batch")
    fun deleteAnswers(@RequestBody entities: List<Answer>): ResponseEntity<Map<String, Boolean>> {
        // 요청 본문의 uid 를 믿지 않고 DB 의 소유자로 확인한다.
        val viewer = viewerId()
        val owned = entities.map { answerService.findById(it.id) }
        if (owned.any { it == null || it.uidId != viewer }) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("success" to false))
        }
        val success = answerService.deleteBatch(owned.filterNotNull())
        return ResponseEntity.ok(mapOf("success" to success))
    }
}