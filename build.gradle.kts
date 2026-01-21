import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktor)
    alias(libs.plugins.updater)
    alias(libs.plugins.kotlin.seriazliation)
    alias(libs.plugins.javacpp)
}

group = "com.dergruenkohl"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.bundles.jda)
    implementation(libs.bundles.database)
    implementation(libs.bundles.kandy)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.misc)
    implementation("org.bytedeco:ffmpeg-platform:7.1-1.5.11")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("WheatBot.jar")
    isZip64 = true
}

kotlin{
    jvmToolchain(21)
    compilerOptions {

    }
}



application {
    mainClass.set("com.dergruenkohl.WheatBot")
}

