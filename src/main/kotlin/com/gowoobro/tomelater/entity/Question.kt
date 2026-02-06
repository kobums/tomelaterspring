package com.gowoobro.tomelater.entity

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "question_tb")
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "q_id")
    val id: Long = 0,
    @Column(name = "q_content")
    val content: String = "",
    @Column(name = "q_month")
    val month: Int = 0,
    @Column(name = "q_day")
    val day: Int = 0,
    @Column(name = "q_createdat")
    val createdat: LocalDateTime? = LocalDateTime.now(),
    @Column(name = "q_updatedat")
    val updatedat: LocalDateTime? = LocalDateTime.now(),
) 

data class QuestionCreateRequest(
    val content: String = "",
    val month: Int = 0,
    val day: Int = 0,
    val createdat: LocalDateTime? = LocalDateTime.now(),
    val updatedat: LocalDateTime? = LocalDateTime.now(),
)

data class QuestionUpdateRequest(
    val id: Long = 0,
    val content: String = "",
    val month: Int = 0,
    val day: Int = 0,
    val createdat: LocalDateTime? = LocalDateTime.now(),
    val updatedat: LocalDateTime? = LocalDateTime.now(),
)

data class QuestionPatchRequest(
    val id: Long = 0,
    val content: String? = null,
    val month: Int? = null,
    val day: Int? = null,
    val createdat: LocalDateTime? = null,
    val updatedat: LocalDateTime? = null,
)



data class QuestionResponse(
    val id: Long,
    val content: String,
    val month: Int,
    val day: Int,
    val createdat: String?,
    val updatedat: String?,

    val extra: Map<String, Any?> = emptyMap()
){
    companion object {
        fun from(question: Question): QuestionResponse {
            return QuestionResponse(
                id = question.id,
                content = question.content,
                month = question.month,
                day = question.day,
                createdat = question.createdat?.toString()?.replace("T", " ") ?: "",
                updatedat = question.updatedat?.toString()?.replace("T", " ") ?: "",

                extra =  emptyMap()
            )
        }
    }
}