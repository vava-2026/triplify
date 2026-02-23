rootProject.name = "triplify"

include("ui", "application", "domain", "infrastructure")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
