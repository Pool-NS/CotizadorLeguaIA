import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) localPropertiesFile.inputStream().use { load(it) }
}
val debugAiBackendUrl = localProperties.getProperty("aiBackendUrl", "http://10.0.2.2:8000")
val releaseAiBackendUrl = localProperties.getProperty("aiBackendUrlRelease", "")
require(releaseAiBackendUrl.isBlank() || releaseAiBackendUrl.startsWith("https://")) {
    "aiBackendUrlRelease must use HTTPS."
}

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
    id("jacoco")
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
    dependsOn("testDebugUnitTest")
    executionData.setFrom(fileTree(layout.buildDirectory) {
        include("jacoco/testDebugUnitTest.exec")
    })
    sourceDirectories.setFrom(files("$projectDir/src/main/java"))
    classDirectories.setFrom(fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) {
        exclude("**/R.class", "**/R$*.class", "**/BuildConfig.class", "**/Manifest.class")
    })
    reports {
        xml.required.set(true)
        xml.outputLocation.set(rootProject.layout.projectDirectory.file("qa/coverage/jacoco.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/testDebugUnitTestCoverage/html"))
        csv.required.set(false)
    }
}

android {
    namespace = "com.leguia.cotizadoria"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.leguia.cotizadoria"
        minSdk = 24
        targetSdk = 35
        versionCode = 7
        versionName = "1.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Local emulator by default; override in ignored local.properties for a phone/ngrok tunnel.
            buildConfigField("String", "AI_BACKEND_URL", "\"${debugAiBackendUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "AI_BACKEND_URL", "\"${releaseAiBackendUrl.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Persistencia local tipada con Room y SQLite.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
