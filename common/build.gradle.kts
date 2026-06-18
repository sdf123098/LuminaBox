plugins {
    id("fabric-loom")
    id("maven-publish")
}

base {
    archivesName.set("luminabox-common")
}

repositories {
    maven("https://maven.aliyun.com/repository/public")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    // Comprehensive Audio Decoder: LavaPlayer
    implementation("dev.arbjerg:lavaplayer:2.2.6")
    implementation("dev.arbjerg:lavaplayer-ext-youtube-rotator:2.2.2")
    implementation("io.github.kyokusakin:bilibili-plugin:1.0.3")

    val patchedJars = fileTree("${project.rootDir}/neoforge/build/moddev/artifacts") {
        include("minecraft-patched-*.jar")
        exclude("*-sources.jar")
    }
    println("=== PATCHED JARS ===")
    patchedJars.forEach { println(it.absolutePath) }

    compileOnly(patchedJars)

    compileOnly("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    compileOnly("net.neoforged:neoforge:${property("neoforge_version")}")
    compileOnly("org.spongepowered:mixin:0.8.5")
}

loom {
    mixin {
        defaultRefmapName = "luminabox-refmap.json"
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
}
