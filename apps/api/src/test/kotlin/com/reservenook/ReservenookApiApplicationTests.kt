package com.reservenook

import com.ninjasquad.springmockk.MockkBean
import com.reservenook.registration.application.RegistrationMailSender
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ReservenookApiApplicationTests {

    @MockkBean
    private lateinit var registrationMailSender: RegistrationMailSender

    @Test
    fun `application context loads`() {
        true.shouldBe(true)
    }
}
