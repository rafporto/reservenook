plugins {
    kotlin("jvm") version "2.1.21"
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

