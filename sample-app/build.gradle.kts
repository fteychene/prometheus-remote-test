plugins {
    kotlin("jvm") version "1.6.10"
    java
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.google.cloud.tools.jib") version "3.2.0"
}

group = "xyz.fteychene.teaching.metric"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(platform("org.http4k:http4k-bom:4.19+"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-format-jackson")
    implementation("org.http4k:http4k-security-oauth")
    implementation("org.http4k:http4k-metrics-micrometer")
    implementation("org.http4k:http4k-contract")
    implementation("org.webjars:swagger-ui:4.2.1")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.2")
    implementation("org.apache.kafka:kafka-clients:2.8.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(platform("com.fasterxml.jackson:jackson-bom:2.12.1"))
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")
    implementation(platform("io.arrow-kt:arrow-stack:1.0.0"))
    implementation("io.arrow-kt:arrow-core")
    implementation("io.arrow-kt:arrow-fx-coroutines")
    implementation("io.arrow-kt:arrow-fx-stm")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    runtimeOnly("org.slf4j:slf4j-api:1.7.35")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.10")
    implementation("org.slf4j:jul-to-slf4j:1.7.35") // Undertow and Vault log through JUL
    implementation("com.bettercloud:vault-java-driver:5.1.0")
    val hopliteVersion = "1.4.16"
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("com.sksamuel.hoplite:hoplite-yaml:$hopliteVersion")
    implementation("io.github.reactivecircus.cache4k:cache4k:0.4.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    val kotestVersion = "5.1.0"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:1.2.0")
    testImplementation("io.kotest.extensions:kotest-extensions-testcontainers:1.1.0")
    val testContainerVersion = "1.16.3"
    testImplementation("org.testcontainers:testcontainers:$testContainerVersion")
    testImplementation("org.testcontainers:kafka:$testContainerVersion")
    testImplementation("org.testcontainers:vault:$testContainerVersion")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        targetCompatibility = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

project.setProperty("mainClassName", "xyz.fteychene.teaching.metric.MainKt")

jib {
    from {
        image = "openjdk:17-slim-buster"
    }
    to {
        image = "startree/prometheus-sample-app"
        tags = setOf("1.0.0")
    }
    container {
        mainClass = project.properties["mainClassName"] as String
        format = com.google.cloud.tools.jib.api.buildplan.ImageFormat.OCI
        jvmFlags = listOf(
            "-XX:+UseContainerSupport",
            "-XX:MinRAMPercentage=50",
            "-XX:MaxRAMPercentage=80"
        )
    }
}