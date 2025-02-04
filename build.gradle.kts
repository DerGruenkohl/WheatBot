import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
val logback_version: String by project
val ktor_version: String by project
val exposedVersion: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    application
}

group = "de.meow"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://m2.duncte123.dev/releases")
    maven("https://packages.jetbrains.team/maven/p/kds/kotlin-ds-maven")
    maven ("https://m2.chew.pro/snapshots")
    maven("https://jitpack.io")
    maven("https://repo.kotlin.link")
}

dependencies {
    testImplementation(kotlin("test"))
    //DISCORD
    implementation("net.dv8tion:JDA:5.3.0")
    implementation("io.github.freya022:BotCommands:3.0.0-alpha.24")
    implementation("dev.freya02:jda-emojis:2.0.0")

    //DATABASE
    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.1")
    implementation("org.flywaydb:flyway-core:11.2.0")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("com.h2database:h2:2.3.232")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    //HTTP
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    //PLOTTING
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.6.0")
    implementation("org.jetbrains.kotlinx:kotlin-statistics-jvm:0.2.1")

    //UTILS
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.4.8")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("WheatBot.jar")
}

kotlin{
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

application {
    mainClass.set("com.dergruenkohl.MainKt")
}