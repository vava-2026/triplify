plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))

    runtimeOnly("ch.qos.logback:logback-classic:1.5.32")
}

application {
    mainClass.set("com.triplify.bootstrap.Launcher")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=javafx.graphics",
        "--enable-native-access=ALL-UNNAMED"
    )
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}
