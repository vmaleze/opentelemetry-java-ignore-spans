pluginManagement {
    plugins {
        id("org.springframework.boot") version "3.0.5"
        id("io.spring.dependency-management") version "1.1.0"
        id("de.undercouch.download") version "5.4.0"
        id("com.github.johnrengelman.shadow") version "8.1.1"
        id("com.google.cloud.tools.jib") version "3.3.1"
    }
}

rootProject.name = "opentelemetry-java-ignore-monitoring-spans"

include("extension")
include("springboot-actuator-smoke-test")
