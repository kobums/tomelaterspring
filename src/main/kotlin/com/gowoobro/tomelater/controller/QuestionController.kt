package com.gowoobro.tomelater.controller

import com.gowoobro.tomelater.entity.Question
import com.gowoobro.tomelater.entity.QuestionCreateRequest
import com.gowoobro.tomelater.entity.QuestionUpdateRequest
import com.gowoobro.tomelater.entity.QuestionPatchRequest
import com.gowoobro.tomelater.service.QuestionService
import com.gowoobro.tomelater.entity.QuestionResponse

import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/question")
class QuestionController(
    private val questionService: QuestionService) {

    private fun toResponse(question: Question): QuestionResponse {
        return QuestionResponse.from(question)
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
    fun getQuestions(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") pagesize: Int,
        @RequestParam(required = false) content: String?,
        @RequestParam(required = false) month: Int?,
        @RequestParam(required = false) day: Int?,
        @RequestParam(required = false) startcreatedat: LocalDateTime?,
        @RequestParam(required = false) endcreatedat: LocalDateTime?,
        @RequestParam(required = false) startupdatedat: LocalDateTime?,
        @RequestParam(required = false) endupdatedat: LocalDateTime?,
    ): ResponseEntity<Map<String, Any>> {
        var results = if (content != null || month != null || day != null || startcreatedat != null || endcreatedat != null || startupdatedat != null || endupdatedat != null || false) {
            var filtered = questionService.findAll(0, Int.MAX_VALUE).content
            if (content != null) {
                filtered = filtered.filter { it.content == content }
            }
            if (month != null) {
                filtered = filtered.filter { it.month == month }
            }
            if (day != null) {
                filtered = filtered.filter { it.day == day }
            }
            if (startcreatedat != null || endcreatedat != null) {
                filtered = filtered.filter { filterByDateRange(it.createdat, startcreatedat, endcreatedat) }
            }
            if (startupdatedat != null || endupdatedat != null) {
                filtered = filtered.filter { filterByDateRange(it.updatedat, startupdatedat, endupdatedat) }
            }
            filtered
        } else {
            questionService.findAll(0, Int.MAX_VALUE).content
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
    fun getQuestion(@PathVariable id: Long): ResponseEntity<QuestionResponse> {
        val res = questionService.findById(id)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }


    @GetMapping("/search/content")
    fun getQuestionByContent(@RequestParam content: String): ResponseEntity<List<QuestionResponse>> {
        val res = questionService.findByContent(content)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/month")
    fun getQuestionByMonth(@RequestParam month: Int): ResponseEntity<List<QuestionResponse>> {
        val res = questionService.findByMonth(month)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/day")
    fun getQuestionByDay(@RequestParam day: Int): ResponseEntity<List<QuestionResponse>> {
        val res = questionService.findByDay(day)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/createdat")
    fun getQuestionByCreatedat(@RequestParam createdat: LocalDateTime): ResponseEntity<List<QuestionResponse>> {
        val res = questionService.findByCreatedat(createdat)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }

    @GetMapping("/search/updatedat")
    fun getQuestionByUpdatedat(@RequestParam updatedat: LocalDateTime): ResponseEntity<List<QuestionResponse>> {
        val res = questionService.findByUpdatedat(updatedat)
        return ResponseEntity.ok(res.map { toResponse(it) } )
    }


    @GetMapping("/count")
    fun getCount(): ResponseEntity<Map<String, Long>> {
        val count = questionService.count()
        return ResponseEntity.ok(mapOf("count" to count))
    }

    @PostMapping
    fun createQuestion(@RequestBody request: QuestionCreateRequest): ResponseEntity<QuestionResponse> {
        return try {
            val res = questionService.create(request)
            ResponseEntity.ok(toResponse(res))
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PostMapping("/batch")
    fun createQuestions(@RequestBody requests: List<QuestionCreateRequest>): ResponseEntity<List<QuestionResponse>> {
        return try {
            val res = questionService.createBatch(requests)
            return ResponseEntity.ok(res.map { toResponse(it) } )
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{id}")
    fun updateQuestion(
        @PathVariable id: Long,
        @RequestBody request: QuestionUpdateRequest
    ): ResponseEntity<QuestionResponse> {
        val updatedRequest = request.copy(id = id)
        val res = questionService.update(updatedRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PatchMapping("/{id}")
    fun patchQuestion(
        @PathVariable id: Long,
        @RequestBody request: QuestionPatchRequest
    ): ResponseEntity<QuestionResponse> {
        val patchRequest = request.copy(id = id)
        val res = questionService.patch(patchRequest)
        return if (res != null) {
            ResponseEntity.ok(toResponse(res))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{id}")
    fun deleteQuestion(@PathVariable id: Long): ResponseEntity<Map<String, Boolean>> {
        val success = questionService.deleteById(id)
        return ResponseEntity.ok(mapOf("success" to success))
    }

    @DeleteMapping("/batch")
    fun deleteQuestions(@RequestBody entities: List<Question>): ResponseEntity<Map<String, Boolean>> {
        val success = questionService.deleteBatch(entities)
        return ResponseEntity.ok(mapOf("success" to success))
    }
}