import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
//    alias(libs.plugins.composeHotReload)
  //    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    id("com.google.gms.google-services")
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }
    }
    
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }
    }

    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kvault)
        }
        commonMain.dependencies {

            // UI
            implementation(libs.coil.compose)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Firebase
            implementation(libs.firebase.app)
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)

            // Data & Storage
            implementation(libs.kotlinx.serialization)



        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "org.alonsitos.chat"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.alonsitos.chat"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.alonsitos.chat.MainKt"
        // Ensure modules are available during development run
        jvmArgs += listOf(
            "--add-modules", "java.management,jdk.management",
            "--add-opens", "java.base/java.lang=ALL-UNNAMED"
        )

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AlonsitosChat"
            packageVersion = "1.0.0"

            // Include essential modules for Firebase and networking
            modules("java.management", "jdk.management", "java.naming", "java.desktop", "jdk.unsupported", "java.instrument")
            includeAllModules = false 

            macOS {
                bundleID = "org.alonsitos.chat"
                dockName = "Alonsitos Chat"
                // Ensure modules are available in the packaged app
                jvmArgs("--add-modules", "java.management,jdk.management")
            }

            windows {
                menu = true
                shortcut = true
            }

            buildTypes.release.proguard {
                isEnabled.set(false) // Disable obfuscation to prevent Firebase runtime issues
            }
        }
    }
}
