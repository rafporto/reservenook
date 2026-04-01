package com.reservenook.auth.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.reservenook.auth.application.PasswordResetMailSender
import com.reservenook.registration.application.RegistrationMailSender
import io.mockk.justRun
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest(properties = ["app.security.hsts-enabled=true"])
@AutoConfigureMockMvc
class SecurityHeadersHstsTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @MockkBean
    private lateinit var passwordResetMailSender: PasswordResetMailSender

    @BeforeEach
    fun configureMailMocks() {
        justRun { registrationMailSender.sendActivationEmail(any(), any(), any()) }
        justRun { passwordResetMailSender.sendPasswordResetEmail(any(), any(), any()) }
    }

    @Test
    fun `secure api requests include hsts when enabled`() {
        mockMvc.post("/api/public/auth/login") {
            secure = true
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(
                LoginRequest(
                    email = "missing@acme.com",
                    password = "WrongPass123"
                )
            )
        }
            .andExpect {
                status { isUnauthorized() }
                header { string("Strict-Transport-Security", containsString("max-age=31536000")) }
                header { string("Strict-Transport-Security", containsString("includeSubDomains")) }
            }
    }
}
