plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.omniafitalimentacion"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.omniafitalimentacion"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // 1. Retrofit: Para conectar tu app con la API de alimentación (Spoonacular, Edamam, etc.)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // 2. Converter Gson: Para que Retrofit traduzca automáticamente los JSON de la API a tus clases de Kotlin
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // 3. OkHttp Logging Interceptor: Opcional pero imprescindible para desarrollo.
    // Te permite ver en el Logcat exactamente qué peticiones haces y qué te devuelve la API.
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 4. Coil: La librería estándar de Compose para cargar imágenes desde una URL de internet
    // (la usaremos para mostrar las fotos de los platos en la tercera pantalla)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // 5. Lifecycle y ViewModel para Compose: Para que tu pantalla pueda reaccionar
    // a los cambios de estado del ViewModel (como pasar de 0 dietas a 1+ dietas)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
}