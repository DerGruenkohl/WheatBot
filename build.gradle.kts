import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktor)
    alias(libs.plugins.updater)
    alias(libs.plugins.kotlin.seriazliation)
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

    implementation(libs.bundles.jda)
    implementation(libs.bundles.database)
    implementation(libs.bundles.kandy)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.misc)
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