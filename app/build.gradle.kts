plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.headline.news"
    compileSdk = 35
    testBuildType = providers.gradleProperty("testBuildType").getOrElse("debug")

    val releaseStore = providers.environmentVariable("SPATIAL_RELEASE_STORE_FILE").orNull
    val releaseStorePassword = providers.environmentVariable("SPATIAL_RELEASE_STORE_PASSWORD").orNull
    val releaseAlias = providers.environmentVariable("SPATIAL_RELEASE_KEY_ALIAS").orNull
    val releaseKeyPassword = providers.environmentVariable("SPATIAL_RELEASE_KEY_PASSWORD").orNull
    val releaseCredentials = listOf(releaseStore, releaseStorePassword, releaseAlias, releaseKeyPassword)
    require(releaseCredentials.all { it == null } || releaseCredentials.all { !it.isNullOrBlank() }) {
        "Set all four SPATIAL_RELEASE_* signing variables, or none for an unsigned build."
    }
    if (releaseStore != null) {
        signingConfigs.create("release") {
            storeFile = file(releaseStore)
            storePassword = releaseStorePassword
            keyAlias = releaseAlias
            keyPassword = releaseKeyPassword
        }
    }

    defaultConfig {
        applicationId = "com.headline.news"
        minSdk = 35
        targetSdk = 35
        versionCode = 6
        versionName = "1.4.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk { abiFilters.add("arm64-v8a") }
    }

    buildTypes {
        release {
            isDebuggable = false
            signingConfig = signingConfigs.findByName("release")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.spatial.bom))
    implementation(libs.spatial.core)
    implementation(libs.spatial.ui.platform)
    implementation(libs.spatial.ui.foundation)
    implementation(libs.spatial.ui.design)
    implementation(libs.spatial.ui.sense)
    implementation(libs.spatial.ui.tracking)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling.preview)
}

configurations.all {
    resolutionStrategy {
        exclude("androidx.compose.ui", "ui")
        exclude("androidx.compose.ui", "ui-graphics")
        exclude("androidx.compose.ui", "ui-text")
        exclude("androidx.compose.foundation", "foundation")
    }
}
