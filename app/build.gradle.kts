plugins {
    id("com.android.application")
    id("com.google.gms.google-services") version "4.4.2"
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.nhom4android"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // BOM để quản lý phiên bản Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))

    // Firebase Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Storage
    implementation("com.google.firebase:firebase-storage")

    // Glide để tải ảnh
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.activity)
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:20.0.6")

    // Optional: Picasso nếu bạn cần
    implementation("com.squareup.picasso:picasso:2.71828")

    // Các thư viện Android cơ bản
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Thư viện kiểm thử
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
