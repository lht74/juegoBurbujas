// ruta: build.gradle.kts (raíz del proyecto)
// ruta: build.gradle.kts (raíz del proyecto)
plugins {
    alias(libs.plugins.android.application) apply false // Uses agp version from libs.versions.toml
    alias(libs.plugins.kotlin.android) apply false    // Uses kotlin version from libs.versions.toml
    alias(libs.plugins.kotlin.compose) apply false     // Uses kotlin version from libs.versions.toml
}