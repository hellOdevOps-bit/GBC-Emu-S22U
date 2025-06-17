plugins {
    id("com.android.application")
    kotlin("android")
    id("de.mannodermaus.android-junit5")
}

android {
    namespace = "com.hello_dev0ps.gbcemus22u"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hello_dev0ps.gbcemus22u"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

dependencies {
    // Kotlin + Compose
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")

    // Unit test JUnit 5 (Cursor-style)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation(kotlin("test"))

    // Android test libs (tu peux les laisser ou les virer si inutiles pour l’instant)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}