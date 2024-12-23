plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.attendex"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.attendex"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    dependencies {
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.activity:activity:1.9.2")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
        implementation("androidx.navigation:navigation-fragment-ktx:2.6.0")
        implementation("androidx.navigation:navigation-ui-ktx:2.6.0")
        implementation("com.google.firebase:firebase-firestore:25.1.1")  // Keep this
        implementation("com.google.firebase:firebase-storage:20.2.1")  // Keep this
        testImplementation("junit:junit:4.13.2")
        implementation("com.github.bumptech.glide:glide:4.16.0")  // Glide dependency (latest version)
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

        // Remove the following conflicting KTX dependencies
        // implementation("com.google.firebase:firebase-firestore-ktx:24.4.1")
        // implementation("com.google.firebase:firebase-storage-ktx:20.1.0")
        // annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
        implementation("com.github.bumptech.glide:glide:4.12.0")  // Glide 4.12.0 (older version)
    }




}
