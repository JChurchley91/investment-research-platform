val ktorVersion: String by project
val logBackVersion: String by project

plugins {
    kotlin("jvm") version "2.1.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logBackVersion")
    implementation("org.slf4j:slf4j-api:1.7.32")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}