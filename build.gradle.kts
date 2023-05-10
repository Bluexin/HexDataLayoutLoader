import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.20"
}

group = "be.bluexin"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0")
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0-beta-28")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.0")
    runtimeOnly("org.slf4j:slf4j-api:2.0.7")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.7")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}
