pluginManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.minecraftforge.net/releases")
        maven("https://maven.shedaniel.me/")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "fabric-loom") {
                useModule("net.fabricmc:fabric-loom:1.7-SNAPSHOT")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
        mavenCentral()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.minecraftforge.net/releases")
        maven("https://libraries.minecraft.net")
        maven("https://maven.shedaniel.me/")
    }
}

rootProject.name = "luminabox"

include("common")
include("fabric")
include("neoforge")
