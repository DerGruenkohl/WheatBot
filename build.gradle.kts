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
val osName = System.getProperty("os.name")
val targetOs = when {
    osName == "Mac OS X" -> "macos"
    osName.startsWith("Win") -> "windows"
    osName.startsWith("Linux") -> "linux"
    else -> error("Unsupported OS: $osName")
}

val osArch = System.getProperty("os.arch")
val targetArch = when (osArch) {
    "x86_64", "amd64" -> "x64"
    "aarch64" -> "arm64"
    else -> error("Unsupported arch: $osArch")
}
val target = "${targetOs}-${targetArch}"

dependencies {
    testImplementation(kotlin("test"))

    implementation(libs.bundles.jda)
    implementation(libs.bundles.database)
    implementation(libs.bundles.kandy)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.misc)
    implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:0.9.3")
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