import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
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