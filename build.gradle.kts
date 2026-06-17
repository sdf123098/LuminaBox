plugins {
    java
    id("net.fabricmc.fabric-loom") version "1.17.11" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
}

subprojects {
    apply(plugin = "java")

    version = project.property("mod_version") as String

    java {
        val javaVersion = (project.property("java_version") as String).toInt()
        toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
        withSourcesJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        val javaVersion = (project.property("java_version") as String).toInt()
        options.release.set(javaVersion)
    }

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.shedaniel.me/")
        maven("https://libraries.minecraft.net/")
    }
}
