plugins {
    id("com.android.application")
    id("com.chaquo.python")
}

android {
    namespace = "com.aiautoclicker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aiautoclicker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-AI"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
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
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
}

chaquopy {
    defaultConfig {
        version = "3.11"
        buildPython("/usr/bin/python3")
        
        pip {
            install("numpy")
            install("opencv-python")
            install("pillow")
            install("pytesseract")
            install("scikit-learn")
            install("scipy")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    implementation("com.rmtheis:tess-two:9.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.github.yhaolpz:FloatWindow:1.0.9")
    implementation("com.guolindev.permissionx:permissionx:1.7.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
