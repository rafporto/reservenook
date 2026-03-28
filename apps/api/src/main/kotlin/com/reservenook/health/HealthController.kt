package com.reservenook.health

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class HealthResponse(
    val application: String,
    val status: String
)

@RestController
@RequestMapping("/api/public")
class HealthController {

    @GetMapping("/ping")
    fun ping(): HealthResponse = HealthResponse(
        application = "reservenook-api",
        status = "ok"
    )
}
