import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val logback_version: String by project
val ktor_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    application
}

group = "de.meow"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.hypixel.net/repository/Hypixel/")
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test"))
    implementation("net.dv8tion:JDA:5.0.0-beta.21")
    implementation("net.hypixel:hypixel-api-transport-reactor:4.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.projectreactor:reactor-core:3.6.5")
    implementation("io.projectreactor.netty:reactor-netty-core:1.1.18")
    implementation("io.projectreactor.netty:reactor-netty-http:1.1.18")
    implementation("com.konghq:unirest-java:3.11.09:standalone")
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("MainKt")
}