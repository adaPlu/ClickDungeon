plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.clickdungeon"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.clickdungeon"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "0.06"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all {
            it.systemProperty("robolectric.enabledSdks", "34")
        }
    }
}

dependencies {

    implementation(libs.gson)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.security.crypto)
    testImplementation(libs.junit)
    testImplementation(libs.test.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
