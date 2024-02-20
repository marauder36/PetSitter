// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id ("com.android.library") version ("7.3.1") apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.20" apply false
    kotlin("kapt") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply true
}

buildscript {

    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("androidx.navigation.safeargs:androidx.navigation.safeargs.gradle.plugin:2.7.5")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
    repositories {
        mavenCentral()
    }
}