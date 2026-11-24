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
