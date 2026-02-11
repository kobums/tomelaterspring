package com.gowoobro.tomelater.controller

import com.gowoobro.tomelater.entity.Answer
import com.gowoobro.tomelater.entity.AnswerCreateRequest
import com.gowoobro.tomelater.entity.AnswerUpdateRequest
import com.gowoobro.tomelater.entity.AnswerPatchRequest
import com.gowoobro.tomelater.service.AnswerService
import com.gowoobro.tomelater.entity.AnswerResponse
import com.gowoobro.tomelater.enums.answer.Ispublic

import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/answer")
class AnswerController(
    private val answerService: AnswerService) {

    private fun toResponse(answer: Answer): AnswerResponse {
        return AnswerResponse.from(answer)
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
    fun getAnswer(@PathVariable id: Long): ResponseEntity<AnswerResponse> {
        val res = answerService.findById(id)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }


    @GetMapping("/search/uid")
    fun getAnswerByUid(@RequestParam uid: Long): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByUid(uid)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/qid")
    fun getAnswerByQid(@RequestParam qid: Long): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByQid(qid)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/content")
    fun getAnswerByContent(@RequestParam content: String): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByContent(content)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/ispublic")
    fun getAnswerByIspublic(@RequestParam ispublic: Ispublic): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByIspublic(ispublic)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/createdat")
    fun getAnswerByCreatedat(@RequestParam createdat: LocalDateTime): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByCreatedat(createdat)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/updatedat")
    fun getAnswerByUpdatedat(@RequestParam updatedat: LocalDateTime): ResponseEntity<List<AnswerResponse>> {
        val res = answerService.findByUpdatedat(updatedat)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }


    @GetMapping("/count")
    fun getCount(): ResponseEntity<Map<String, Long>> {
        val count = answerService.count()
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PostMapping
    fun createAnswer(@RequestBody request: AnswerCreateRequest): ResponseEntity<AnswerResponse> {
        return try {
            val res = answerService.create(request)
            ResponseEntity.ok(toResponse(res))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/batch")
    fun createAnswers(@RequestBody requests: List<AnswerCreateRequest>): ResponseEntity<List<AnswerResponse>> {
        return try {
            val res = answerService.createBatch(requests)
            return ResponseEntity.ok(res.map { toResponse(it) } )
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updateAnswer(
        @PathVariable id: Long,
        @RequestBody request: AnswerUpdateRequest
    ): ResponseEntity<AnswerResponse> {
        val updatedRequest = request.copy(id = id)
        val res = answerService.update(updatedRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}")
    fun patchAnswer(
        @PathVariable id: Long,
        @RequestBody request: AnswerPatchRequest
    ): ResponseEntity<AnswerResponse> {
        val patchRequest = request.copy(id = id)
        val res = answerService.patch(patchRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteAnswer(@PathVariable id: Long): ResponseEntity<Map<String, Boolean>> {
        val success = answerService.deleteById(id)
        return ResponseEntity.ok(mapOf("success" to success))
    }

    @DeleteMapping("/batch")
    fun deleteAnswers(@RequestBody entities: List<Answer>): ResponseEntity<Map<String, Boolean>> {
        val success = answerService.deleteBatch(entities)
        return ResponseEntity.ok(mapOf("success" to success))
    }
}