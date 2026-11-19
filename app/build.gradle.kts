plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.groundstudio.topohit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.groundstudio.topohit"
        minSdk = 23                     // Android 6.0, como en clase
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
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
    // ViewBinding (vídeo 39): adiós a findViewById
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    // Retrofit + GSON para la API (vídeos 35-38)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    // Corrutinas para GlobalScope.launch (vídeo 35)
    implementation(libs.kotlinx.coroutines.android)
    // [No visto en clase] jBCrypt: cifra la contraseña con bcrypt antes de enviarla a la API
    // (reto "Protecció dels usuaris"). Es la misma librería que BCrypt.Net del escritorio.
    implementation(libs.jbcrypt)
}
