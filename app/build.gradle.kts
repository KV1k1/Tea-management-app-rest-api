plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tea_quarkus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tea_quarkus"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Network dependencies - ONLY THESE ARE NEEDED FOR ANDROID CLIENT
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // RecyclerView for your tea list
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // REMOVE THESE QUARKUS DEPENDENCIES - THEY ARE FOR SERVER ONLY!
    // implementation ("io.quarkus:quarkus-smallrye-jwt")
    // implementation ("io.quarkus:quarkus-security-jdbc")
    // implementation ("io.quarkus:quarkus-hibernate-orm-panache")
    // implementation ("io.quarkus:quarkus-jdbc-postgresql")
}