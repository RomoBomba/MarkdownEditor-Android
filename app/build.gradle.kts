plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.markdowneditorv2"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.markdowneditorv2"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperty("robolectric.enabled", "true")
                it.jvmArgs("-noverify", "--add-opens=java.base/java.lang=ALL-UNNAMED")
                it.maxHeapSize = "2048m" // Увеличьте память

                // Обновленные настройки для Robolectric
                it.systemProperty("robolectric.offline", "false") // Changed to false
                it.systemProperty("robolectric.dependency.repo.url", "https://repo1.maven.org/maven2")
                it.systemProperty("robolectric.dependency.repo.id", "central")
                it.systemProperty("robolectric.logging.enabled", "true")
            }
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation("org.robolectric:robolectric:4.13")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:core:1.5.0")
}