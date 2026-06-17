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
