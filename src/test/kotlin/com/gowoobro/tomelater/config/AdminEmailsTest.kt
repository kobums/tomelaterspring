package com.gowoobro.tomelater.config

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class AdminEmailsTest {

    @AfterEach
    fun clear() = SecurityContextHolder.clearContext()

    @Test
    fun `parses comma separated list case-insensitively`() {
        assertEquals(setOf("a@x.com", "b@y.com"), AdminEmails.parse(" A@x.com, ,b@Y.com ,"))
    }

    @Test
    fun `unset refuses everyone`() {
        val admins = AdminEmails("")
        assertFalse(admins.isAdmin("a@x.com"))
        assertFalse(admins.isAdmin(""))
        assertFalse(admins.isAdmin(null))
    }

    @Test
    fun `matches listed emails ignoring case and whitespace`() {
        val admins = AdminEmails("Admin@Example.com")
        assertTrue(admins.isAdmin(" admin@example.COM "))
        assertFalse(admins.isAdmin("other@example.com"))
    }

    @Test
    fun `checks the authenticated principal`() {
        val admins = AdminEmails("admin@example.com")
        assertFalse(admins.isCurrentUserAdmin())

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("user@example.com", null, emptyList())
        assertFalse(admins.isCurrentUserAdmin())

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken("ADMIN@example.com", null, emptyList())
        assertTrue(admins.isCurrentUserAdmin())
    }
}
