import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.zivaa.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.zivaa.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 33
        versionName = "1.0.32"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("zivaa-release.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "zivaa123"
                keyAlias = "zivaa"
                keyPassword = "zivaa123"
            }
        }
    }

    buildTypes {
        debug {
            val properties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                properties.load(FileInputStream(localPropertiesFile))
            }
            buildConfigField("String", "API_BASE_URL", properties.getProperty("API_BASE_URL", "\"https://zivaa-backend-121011128860.europe-west4.run.app/\""))
            resValue("string", "app_name", "Zivaa")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://zivaa-backend-121011128860.europe-west4.run.app/\"")
            resValue("string", "app_name", "Zivaa")
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            firebaseAppDistribution {
                serviceCredentialsFile = rootProject.file("../zivaa-backend/serviceAccountKey.json").absolutePath
                groups = "zivaa-beta"
                releaseNotes = "Zivaa v1.0.32 - Background health data read permission for continuous background sync and telemetry stability"
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Health Connect Client
    implementation("androidx.health.connect:connect-client:1.1.0")

    // WorkManager (Kotlin + Coroutines)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Security (EncryptedSharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Firebase / Push Messaging & App Distribution
    implementation("com.google.firebase:firebase-messaging:23.4.1")
    implementation("com.google.firebase:firebase-appdistribution-api-ktx:16.0.0-beta14")
    releaseImplementation("com.google.firebase:firebase-appdistribution:16.0.0-beta14")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Vico Charting
    implementation("com.patrykandpatrick.vico:compose:1.15.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")
    implementation("com.patrykandpatrick.vico:core:1.15.0")
    
    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // ML Kit Barcode
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // CameraX
    val camerax_version = "1.3.1"
    implementation("androidx.camera:camera-core:${camerax_version}")
    implementation("androidx.camera:camera-camera2:${camerax_version}")
    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation("androidx.camera:camera-view:${camerax_version}")

    implementation("com.google.guava:guava:31.1-android")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    
    // Room
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
}

ksp {
    arg("room.generateKotlin", "true")
}

