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
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://jitpack.io")
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
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")

    //HTTP
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")

    //PLOTTING
    implementation("org.jetbrains.kotlinx:kandy-lets-plot:0.7.1")

    //UTILS
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("dev.reformator.stacktracedecoroutinator:stacktrace-decoroutinator-jvm:2.4.8")
    implementation("io.github.dergruenkohl:Hypixel-Kotlin:0.1.5")

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