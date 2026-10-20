package com.gowoobro.tomelater.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

/**
 * 질문(question_tb) 작성·수정·삭제를 허용할 관리자 이메일 목록.
 *
 * ADMIN_EMAILS(콤마 구분, 대소문자 무시)로 준다. 비어 있으면 질문 쓰기는 전부 거부된다(읽기는 그대로).
 * 예전엔 로그인한 아무나 오늘의 질문을 고치거나 지울 수 있었다. 질문은 questions.json 시드로 채워지고
 * 앱·웹은 조회만 하므로 쓰기는 관리자 전용으로 둔다. (tomelatergo 의 requireAdmin 과 동일한 규칙)
 */
@Component
class AdminEmails(@Value("\${ADMIN_EMAILS:}") raw: String) {

    val emails: Set<String> = parse(raw)

    init {
        if (emails.isEmpty()) {
            log.warn("ADMIN_EMAILS 미설정: 질문 작성·수정·삭제 API 는 모두 403 (질문은 questions.json 시드로만 채워진다)")
        }
    }

    fun isAdmin(email: String?): Boolean {
        val e = email?.trim()?.lowercase().orEmpty()
        return e.isNotEmpty() && e in emails
    }

    /** 현재 요청의 JWT 사용자(이메일)가 관리자인지. */
    fun isCurrentUserAdmin(): Boolean = isAdmin(SecurityContextHolder.getContext().authentication?.name)

    companion object {
        private val log = LoggerFactory.getLogger(AdminEmails::class.java)

        fun parse(raw: String?): Set<String> =
            raw.orEmpty().split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
    }
}
