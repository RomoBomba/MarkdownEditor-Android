plugins {
    alias(libs.plugins.android.application) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.withType<Wrapper> {
    distributionUrl = "https://services.gradle.org/distributions/gradle-8.5-bin.zip"
}