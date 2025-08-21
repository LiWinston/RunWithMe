val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.2.3"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17" // 或 "11"，取决于你本地安装的 JDK
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // 统一 JDK 版本
    }
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-call-logging:3.2.3")
    implementation("io.ktor:ktor-server-core:3.2.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.2.3")
    implementation("io.ktor:ktor-server-netty:3.2.3")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("io.ktor:ktor-serialization-gson:3.2.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.2.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.2.3")
    implementation("io.ktor:ktor-server-config-yaml:3.2.3")
    testImplementation("io.ktor:ktor-server-test-host:3.2.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
