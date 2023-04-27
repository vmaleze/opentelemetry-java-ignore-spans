import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
	id("java")
	id("org.springframework.boot") version "3.0.5"
	id("io.spring.dependency-management") version "1.1.0"
	id("com.google.cloud.tools.jib") version "3.3.1"
}

group = "io.opentelemetry.smoketests"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val targetJDK = project.findProperty("targetJDK") ?: "17"
val tag = findProperty("tag")
		?: DateTimeFormatter.ofPattern("yyyyMMdd.HHmmSS").format(LocalDateTime.now())

java {
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

jib {
	from.image = "openjdk:$targetJDK"
	from.platforms {
		platform {
			architecture = "amd64"
			os = "linux"
		}
		platform {
			architecture = "arm64"
			os = "linux"
		}
	}
	to.image = "ghcr.io/vmaleze/opentelemetry-java-ignore-monitoring-spans/smoke-test-spring-boot-actuator:jdk$targetJDK-$tag"
	container.ports = listOf("8080")
}

tasks {
	val springBootJar by configurations.creating {
		isCanBeConsumed = true
		isCanBeResolved = false
	}

	artifacts {
		add("springBootJar", bootJar)
	}
}
