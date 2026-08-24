plugins {
    id("com.android.application")
}

android {
    namespace = "dev.akexorcist.samsung.edge2edge.issue"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.akexorcist.samsung.edge2edge.issue"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}
