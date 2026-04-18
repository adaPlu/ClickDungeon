plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
}

android {
    namespace = "com.adaplu.clickdungeon"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.adaplu.clickdungeon"
        minSdk = 21
        targetSdk = 36
        versionCode = 2
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing is configured via ~/.gradle/gradle.properties to keep
    // credentials out of source control. Add the following four properties
    // to that file before running `./gradlew bundleRelease`:
    //
    //   CLICKDUNGEON_STORE_FILE=/absolute/path/to/clickdungeon.jks
    //   CLICKDUNGEON_STORE_PASSWORD=<keystore password>
    //   CLICKDUNGEON_KEY_ALIAS=clickdungeon
    //   CLICKDUNGEON_KEY_PASSWORD=<key password>
    //
    // Generate the keystore once with:
    //   keytool -genkey -v -keystore clickdungeon.jks -alias clickdungeon \
    //           -keyalg RSA -keysize 2048 -validity 10000
    // Then back it up offline — losing it permanently locks the app out of Play.
    signingConfigs {
        create("release") {
            val storeFilePath = project.findProperty("CLICKDUNGEON_STORE_FILE") as String?
            val storePass    = project.findProperty("CLICKDUNGEON_STORE_PASSWORD") as String?
            val keyAlias     = project.findProperty("CLICKDUNGEON_KEY_ALIAS") as String?
            val keyPass      = project.findProperty("CLICKDUNGEON_KEY_PASSWORD") as String?
            if (storeFilePath != null && storePass != null && keyAlias != null && keyPass != null) {
                storeFile = File(storeFilePath)
                storePassword = storePass
                this.keyAlias = keyAlias
                keyPassword = keyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }
    buildFeatures {
        buildConfig = true
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
    // billingclient removed until real IAP is wired in v1.1
    // implementation("com.android.billingclient:billing:5.1.0")
    implementation(libs.material)
    implementation(libs.tink.android)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit)
    testImplementation(libs.test.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

// Compatibility aliases for older docs/scripts that still reference
// offline/online-flavored task names. The project currently builds a
// single app variant, so these redirect to the existing debug tasks.
listOf(
    Triple("assembleOfflineDebug", "assembleDebug", "Build the current debug APK via the legacy offline task name."),
    Triple("assembleOnlineDebug", "assembleDebug", "Build the current debug APK via the legacy online task name."),
    Triple("installOfflineDebug", "installDebug", "Install the current debug APK via the legacy offline task name."),
    Triple("installOnlineDebug", "installDebug", "Install the current debug APK via the legacy online task name."),
    Triple("testOfflineDebugUnitTest", "testDebugUnitTest", "Run the standard unit-test suite via the legacy offline task name."),
    Triple("testOnlineDebugUnitTest", "testDebugUnitTest", "Run the standard unit-test suite via the legacy online task name."),
    Triple("connectedOfflineAndroidTest", "connectedDebugAndroidTest", "Run connected tests via the legacy offline task name."),
    Triple("connectedOnlineDebugAndroidTest", "connectedDebugAndroidTest", "Run connected tests via the legacy online task name.")
).forEach { (aliasName, targetName, taskDescription) ->
    if (tasks.findByName(aliasName) == null) {
        tasks.register(aliasName) {
            group = "verification"
            description = taskDescription
            dependsOn(targetName)
        }
    }
}
