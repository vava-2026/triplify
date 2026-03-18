rootProject.name = "triplify"

include("ui", "application", "domain", "infrastructure", "bootstrap")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
