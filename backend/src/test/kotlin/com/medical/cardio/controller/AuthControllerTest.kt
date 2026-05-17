package com.medical.cardio.controller

import com.medical.cardio.entity.AdminUserEntity
import com.medical.cardio.entity.Role
import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.support.ControllerTestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class AuthControllerTest : ControllerTestBase() {

    @Autowired
    private lateinit var adminUserRepository: AdminUserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var testUser: AdminUserEntity

    @BeforeEach
    fun setUp() {
        testUser = adminUserRepository.save(
            AdminUserEntity(
                email = "auth-test@cardio.ru",
                passwordHash = passwordEncoder.encode("ValidPass@2000"),
                role = Role.DOCTOR
            )
        )
    }

    @AfterEach
    fun tearDown() {
        adminUserRepository.delete(testUser)
    }

    @Test
    fun `login with valid credentials returns 200 and cookie`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2024"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("auth-test@cardio.ru") }
            jsonPath("$.role") { value("ROLE_DOCTOR") }
            header { exists("Set-Cookie") }
        }
    }

    @Test
    fun `login with wrong password returns 401`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"WrongPassword"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `login with invalid email format returns 400`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"not-an-email","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `me with valid token returns 200 with user data`() {
        val cookie = authCookie(testUser)
        mockMvc.get("/api/auth/me") {
            cookie(cookie)
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("auth-test@cardio.ru") }
            jsonPath("$.role") { value("ROLE_DOCTOR") }
        }
    }

    @Test
    fun `me without token returns 401`() {
        mockMvc.get("/api/auth/me").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `logout returns 200 and clears cookie`() {
        mockMvc.post("/api/auth/logout").andExpect {
            status { isOk() }
            header { string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")) }
        }
    }

    @Test
    fun `login with non-existent email returns 401`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"nobody@cardio.ru","password":"ValidPass@2000"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `login with rememberMe true includes Max-Age in Set-Cookie`() {
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"auth-test@cardio.ru","password":"ValidPass@2000","rememberMe":true}"""
        }.andExpect {
            status { isOk() }
            header { string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=2592000")) }
        }
    }
}
