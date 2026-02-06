package com.gowoobro.tomelater.entity

import jakarta.persistence.*
import java.time.LocalDateTime

import com.gowoobro.tomelater.enums.answer.Ispublic

@Entity
@Table(name = "answer_tb")
data class Answer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "a_id")
    val id: Long = 0,
    @Column(name = "a_uid")
    val uidId: Long = 0L,
    @Column(name = "a_qid")
    val qidId: Long = 0L,
    @Column(name = "a_content")
    val content: String = "",
    @Column(name = "a_ispublic")
    val ispublic: Ispublic = Ispublic.PRIVATE,
    @Column(name = "a_createdat")
    val createdat: LocalDateTime? = LocalDateTime.now(),
    @Column(name = "a_updatedat")
    val updatedat: LocalDateTime? = LocalDateTime.now(),
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_uid", insertable = false, updatable = false)
    var user: User? = null
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_qid", insertable = false, updatable = false)
    var question: Question? = null
}

data class AnswerCreateRequest(
    val uid: Long = 0L,
    val qid: Long = 0L,
    val content: String = "",
    val ispublic: Ispublic = Ispublic.PRIVATE,
    val createdat: LocalDateTime? = LocalDateTime.now(),
    val updatedat: LocalDateTime? = LocalDateTime.now(),
)

data class AnswerUpdateRequest(
    val id: Long = 0,
    val uid: Long = 0L,
    val qid: Long = 0L,
    val content: String = "",
    val ispublic: Ispublic = Ispublic.PRIVATE,
    val createdat: LocalDateTime? = LocalDateTime.now(),
    val updatedat: LocalDateTime? = LocalDateTime.now(),
)

data class AnswerPatchRequest(
    val id: Long = 0,
    val uid: Long? = null,
    val qid: Long? = null,
    val content: String? = null,
    val ispublic: Ispublic? = null,
    val createdat: LocalDateTime? = null,
    val updatedat: LocalDateTime? = null,
)

data class AnswerExtraInfo(
    val ispublic: String = "",

    val user: UserResponse? = null,
    val question: QuestionResponse? = null,
)


data class AnswerResponse(
    val id: Long,
    val uid: Long,
    val qid: Long,
    val content: String,
    val ispublic: Int,
    val createdat: String?,
    val updatedat: String?,

    val extra: AnswerExtraInfo
){
    companion object {
        fun from(answer: Answer): AnswerResponse {
            val userResponse = answer.user?.let { UserResponse.from(it) }
            val questionResponse = answer.question?.let { QuestionResponse.from(it) }
            return AnswerResponse(
                id = answer.id,
                uid = answer.uidId,
                qid = answer.qidId,
                content = answer.content,
                ispublic = answer.ispublic.ordinal,
                createdat = answer.createdat?.toString()?.replace("T", " ") ?: "",
                updatedat = answer.updatedat?.toString()?.replace("T", " ") ?: "",

                extra = AnswerExtraInfo(
                    ispublic = Ispublic.getDisplayName(answer.ispublic),
                    user = userResponse,question = questionResponse,)
                
            )
        }
    }
}