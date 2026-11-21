plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "app.hushh.shared"
        compileSdk = 37
        minSdk = 26
        androidResources { enable = true }
        withHostTest {}
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "Shared"
            isStatic = true
            binaryOption("bundleId", "app.hushh.shared")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.core)
            implementation(libs.compose.components.resources)
            implementation(libs.libsodium)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        // Android host tests run on a desktop JVM; the Android libsodium artifact only bundles
        // phone builds of the native library, so add the desktop builds from the JVM artifact.
        getByName("androidHostTest").dependencies {
            runtimeOnly(libs.libsodium.jvm)
        }
    }
}
