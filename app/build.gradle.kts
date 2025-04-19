plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "usc.edu.ph.taskybear"
    compileSdk = 35

    defaultConfig {
        applicationId = "usc.edu.ph.taskybear"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.activity)
    implementation ("com.github.CanHub:Android-Image-Cropper:4.3.2")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
      implementation ("com.github.yalantis:ucrop:2.2.8")
    implementation ("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.cardview:cardview:1.0.0")


    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}