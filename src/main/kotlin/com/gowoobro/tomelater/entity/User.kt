package com.gowoobro.tomelater.entity

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "user_tb")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "u_id")
    val id: Long = 0,
    @Column(name = "u_email")
    val email: String = "",
    @Column(name = "u_passwd")
    val passwd: String = "",
    @Column(name = "u_nickname")
    val nickname: String = "",
    @Column(name = "u_socialtype")
    val socialtype: String = "",
    @Column(name = "u_socialid")
    val socialid: String = "",
    @Column(name = "u_createdat")
    val createdat: LocalDateTime? = LocalDateTime.now(),
    @Column(name = "u_updatedat")
    val updatedat: LocalDateTime? = LocalDateTime.now(),
) 

data class UserCreateRequest(
    val email: String = "",
    val passwd: String = "",
    val nickname: String = "",
    val socialtype: String = "",
    val socialid: String = "",
    val createdat: LocalDateTime? = LocalDateTime.now(),
    val updatedat: LocalDateTime? = LocalDateTime.now(),
)

data class UserUpdateRequest(
    val id: Long = 0,
    val email: String = "",
    val passwd: String = "",
    val nickname: String = "",
    val socialtype: String = "",
    val socialid: String = "",
    val createdat: LocalDateTime? = LocalDateTime.now(),
    val updatedat: LocalDateTime? = LocalDateTime.now(),
)

data class UserPatchRequest(
    val id: Long = 0,
    val email: String? = null,
    val passwd: String? = null,
    val nickname: String? = null,
    val socialtype: String? = null,
    val socialid: String? = null,
    val createdat: LocalDateTime? = null,
    val updatedat: LocalDateTime? = null,
)



data class UserResponse(
    val id: Long,
    val email: String,
    val passwd: String,
    val nickname: String,
    val socialtype: String,
    val socialid: String,
    val createdat: String?,
    val updatedat: String?,

    val extra: Map<String, Any?> = emptyMap()
){
    companion object {
        fun from(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                email = user.email,
                passwd = user.passwd,
                nickname = user.nickname,
                socialtype = user.socialtype,
                socialid = user.socialid,
                createdat = user.createdat?.toString()?.replace("T", " ") ?: "",
                updatedat = user.updatedat?.toString()?.replace("T", " ") ?: "",

                extra =  emptyMap()
            )
        }
    }
}