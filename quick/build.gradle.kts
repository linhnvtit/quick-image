plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id(libs.plugins.maven.publish.get().pluginId)
}

fun Any?.getInt() = this?.toString()?.toIntOrNull()
fun Any?.getString() = this?.toString()

android {
    namespace = properties["namespace"].getString()
    compileSdk = properties["compileSdk"].getInt()

    defaultConfig {
        minSdk = properties["minSdk"].getInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = properties["jvmTarget"].toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = properties["kotlinComposeCompilerExtension"].toString()
    }
}

afterEvaluate {
    android.libraryVariants.forEach { variant ->
        publishing.publications.create(variant.name, MavenPublication::class.java) {
            from(components.findByName(variant.name))

            groupId = properties["publishGroupId"].toString()
            artifactId = properties["publishArtifactId"].toString()
            version = properties["publishVersion"].toString()
        }
    }
}

dependencies {
    implementation(libs.android.material)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle)

    implementation(platform(libs.jp.compose.bom))
    implementation(libs.jp.compose.ui)
    implementation(libs.jp.compose.ui.graphic)
    implementation(libs.jp.compose.material3)
    implementation(libs.jp.compose.runtime)

    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.log)
    implementation(libs.ktor.client.json)
}