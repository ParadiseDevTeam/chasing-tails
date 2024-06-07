import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.*

plugins {
    idea
    alias(libs.plugins.kotlin)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.pluginYml)
}

group = "is.prd"
version = "1.0.0"
val codeName = "chasingtails"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    library(kotlin("stdlib"))
    compileOnly(libs.paper)

    compileOnly(libs.coroutines)
    compileOnly(libs.mccoroutines)
    compileOnly(libs.mccoroutinesCore)

    paperLibrary(libs.coroutines)
    paperLibrary(libs.mccoroutines)
    paperLibrary(libs.mccoroutinesCore)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks {
    jar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set("")
        archiveVersion.set("")
    }
    runServer {
        minecraftVersion("1.20.6")
        jvmArgs = listOf("-Dcom.mojang.eula.agree=true")
    }
}

idea {
    module {
        excludeDirs.addAll(listOf(file("run"), file("out"), file(".idea"), file(".kotlin")))
    }
}

paper {
    name = rootProject.name
    version = rootProject.version.toString()
    author = "Paradise Dev Team"

    main = "${project.group}.${codeName}.plugin.${codeName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}Plugin"
    loader = "${project.group}.${codeName}.plugin.loader.${codeName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}PluginLoader"

    generateLibrariesJson = true
    foliaSupported = false

    apiVersion = "1.20"
}