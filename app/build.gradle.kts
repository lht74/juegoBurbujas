plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.burbujasgame"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.burbujasgame"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    composeOptions {
        // Versión recomendada para compatibilidad con BOM actual
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 🔁 Usamos el BOM de Jetpack Compose para gestionar versiones coherentes
    val composeBom = platform("androidx.compose:compose-bom:2024.02.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // 🎨 UI principal de Jetpack Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview") // Preview en Android Studio
    implementation("androidx.compose.ui:ui-graphics") // Necesario para graphicsLayer
    implementation("androidx.compose.ui:ui-text") // Necesario para TextStyle
    // 🧱 Material Design - Selecciona solo uno:
    implementation("androidx.compose.material3:material3") // Material 3 (recomendado)
    // implementation("androidx.compose.material:material") // Material 1 (opcional si necesitas Text u otros elementos heredados)

    // ⚙️ Animaciones (incluye animateColor, infiniteRepeatable, rememberInfiniteTransition)
    implementation("androidx.compose.animation:animation")

    // 📱 Integración Activity + Compose
    implementation("androidx.activity:activity-compose:1.8.2")

    // 🔄 ViewModel y LiveData con Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // 🗺️ Navegación con Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // 💾 Core de Android
    implementation("androidx.core:core-ktx:1.12.0") // Última versión estable

    // 🧠 Lifecycle runtime para observables
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // 🧵 Kotlin Coroutines para Android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // 🛠️ Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}