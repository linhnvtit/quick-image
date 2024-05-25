pluginManagement {
    repositories {
        google()
        mavenCentral {
            url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
        }
        gradlePluginPortal()
        mavenLocal()
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

val quick: String by settings

include(quick)
