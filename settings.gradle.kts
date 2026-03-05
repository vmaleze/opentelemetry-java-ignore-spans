pluginManagement {
    plugins {
        id("org.springframework.boot") version "3.5.11"
        id("io.spring.dependency-management") version "1.1.7"
        id("de.undercouch.download") version "5.7.0"
        id("com.gradleup.shadow") version "9.3.2"
        id("com.google.cloud.tools.jib") version "3.5.2"
    }
}

rootProject.name = "opentelemetry-java-ignore-spans"

include("extension")
include("springboot-actuator-smoke-test")
