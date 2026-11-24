// Archivo de Gradle del proyecto: solo declara los plugins (se aplican en app/build.gradle.kts)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.chaquopy) apply false
}
