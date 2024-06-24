buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
    }
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.google.com/")
        }
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
/*
val mapkitApiKey: String by lazy {
    loadMapkitApiKey()
}

fun loadMapkitApiKey(): String {
    val properties = Properties()
    project.file("local.properties").inputStream().use { properties.load(it) }
    return properties.getProperty("MAPKIT_API_KEY", "")
}

ext["mapkitApiKey"] = mapkitApiKey*/