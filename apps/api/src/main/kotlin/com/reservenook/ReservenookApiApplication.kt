package com.reservenook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReservenookApiApplication

fun main(args: Array<String>) {
    runApplication<ReservenookApiApplication>(*args)
}
