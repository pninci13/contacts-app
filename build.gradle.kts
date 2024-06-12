buildscript {
    dependencies {
        classpath (libs.google.services.v401)
        classpath(libs.google.services)
        classpath(libs.gradle)
        classpath(libs.firebase.crashlytics.gradle)

        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
    }

    repositories {
        mavenCentral()
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
}
