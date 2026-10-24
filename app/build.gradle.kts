plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "moe.ktandth.songmeld"
    compileSdk = 37

    defaultConfig {
        applicationId = "moe.ktandth.songmeld"

        minSdk = 26
        targetSdk = 37

        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.newpipe.extractor)
    // Back-up SoundCloud audio extractor from the maintained Seal / SongMeld-v1 stack.
    implementation("io.github.junkfood02.youtubedl-android:library:0.18.1")
    // NewPipe Extractor uses newer Java APIs on Android 8 through Android 12.
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.1.5")

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation("junit:junit:4.13.2")
    // Android's org.json is a stub in host unit tests; exercise the real parser on the JVM.
    testImplementation("org.json:json:20240303")
}
