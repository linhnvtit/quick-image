pluginManagement {
    repositories {
        google()
        mavenCentral {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "quickimage"

val app: String by settings
val quick: String by settings

include(app)
include(quick)
