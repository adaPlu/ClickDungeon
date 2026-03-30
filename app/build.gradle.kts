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

    // Include an "online" test source directory so online-only tests
    // placed under `app/src/online/java` are compiled with unit tests.
    sourceSets {
        getByName("test") {
            java.srcDir("src/online/java")
            resources.srcDir("src/online/resources")
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {

    implementation(libs.gson)
    implementation(libs.appcompat)
    implementation("com.android.billingclient:billing:5.1.0")
    implementation(libs.material)
    implementation(libs.tink.android)
    testImplementation(libs.junit)
    testImplementation(libs.test.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
