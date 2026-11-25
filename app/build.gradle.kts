plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Esta parte es de información externa que no sale en los vídeos
    // (Chaquopy: ejecutar Python dentro de la app, lo pide el reto de Métricas para la pestaña «Mètriques»)
    alias(libs.plugins.chaquopy)
}

android {
    namespace = "com.groundstudio.topohit"
    compileSdk = 36                     // 36 para poder usar android:pageSizeCompat en el manifest

    defaultConfig {
        applicationId = "com.groundstudio.topohit"
        minSdk = 24                     // Android 7.0: pandas y matplotlib de Chaquopy lo piden (en clase, 23)
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Los procesadores de las tablets: armeabi-v7a (32 bits, la Samsung Galaxy Tab A de clase) y arm64-v8a,
        // y el del emulador (x86_64). Los demás se dejan fuera para que el APK pese menos
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Esta parte es de información externa que no sale en los vídeos
// (Chaquopy: Python 3.11 con pandas y matplotlib para las gráficas de «Mis estadísticas».
//  3.11 porque es el último que Chaquopy tiene para tablets de 32 bits (la Galaxy Tab A); 3.12 y más solo van en 64 bits.
//  Para compilar hace falta Python 3.11 instalado en el ordenador)
chaquopy {
    defaultConfig {
        version = "3.11"
        pip {
            install("pandas")
            install("matplotlib")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    // Retrofit + Gson para la API (REGLA-0489-067: gson, retrofit y converter-gson)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    // Corrutinas para GlobalScope.launch (REGLA-0489-069)
    implementation(libs.kotlinx.coroutines.android)
    // Esta parte es de información externa que no sale en los vídeos
    // (jBCrypt: el profesor de 0490 da jbcrypt.jar, pero ningún vídeo lo programa).
    // Cifra la contraseña con bcrypt antes de enviarla a la API (reto «Protecció dels usuaris»).
    implementation(libs.jbcrypt)
}
