pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://devrepo.kakaomobility.com/repository/kakao-mobility-android-knsdk-public/")
        maven("https://www.jitpack.io")
    }
}

rootProject.name = "FindLoadSampleProject"
include(":app")
 