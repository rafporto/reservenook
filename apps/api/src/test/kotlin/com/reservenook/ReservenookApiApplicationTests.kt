package com.reservenook

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ReservenookApiApplicationTests {

    @Test
    fun `application context loads`() {
        true.shouldBe(true)
    }
}
