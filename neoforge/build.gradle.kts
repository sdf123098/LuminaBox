plugins {
    id("net.neoforged.moddev")
    id("maven-publish")
}

base {
    archivesName.set("luminabox-neoforge-26.2.x")
}

repositories {
    mavenCentral()
}

neoForge {
    version = property("neoforge_version") as String

    runs {
        create("client") {
            client()
        }
        create("server") {
            server()
        }
    }

    mods {
        register("luminabox") {
            sourceSet(sourceSets.main.get())
        }
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${property("neoforge_version")}")

    // Depend on common module
    implementation(project(":common"))

    // Comprehensive Audio Decoder: LavaPlayer and its transitive dependencies
    val lavaDeps = listOf(
        "dev.arbjerg:lavaplayer:2.2.6",
        "dev.arbjerg:lava-common:2.2.6",
        "dev.arbjerg:lavaplayer-natives:2.2.6",
        "dev.arbjerg:lavaplayer-ext-youtube-rotator:2.2.2",
        "org.apache.httpcomponents:httpclient:4.5.14",
        "org.apache.httpcomponents:httpcore:4.4.16",
        "commons-logging:commons-logging:1.2",
        "com.fasterxml.jackson.core:jackson-core:2.15.2",
        "com.fasterxml.jackson.core:jackson-databind:2.15.2",
        "com.fasterxml.jackson.core:jackson-annotations:2.15.2",
        "org.mozilla:rhino-engine:1.7.14",
        "org.mozilla:rhino:1.7.14",
        "org.jsoup:jsoup:1.16.1",
        "net.iharder:base64:2.3.9",
        "org.json:json:20240303",
        "io.github.kyokusakin:bilibili-plugin:1.0.3",
        "org.jetbrains.kotlin:kotlin-stdlib:1.9.22"
    )

    lavaDeps.forEach { dep ->
        implementation(dep)
        "jarJar"(dep)
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
}

tasks.jar {
    val commonProject = project(":common")
    from(commonProject.sourceSets.main.get().output)
}
