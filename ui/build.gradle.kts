plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":application"))
    implementation(project(":infrastructure"))  // needed for AppModule to install InfrastructureModule

    runtimeOnly("ch.qos.logback:logback-classic:1.5.32")

    // Icon library – Ikonli with Feather pack
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-feather-pack:12.4.0")

    // Routing
    implementation("com.github.rahulstech:javafx-routing:2.0.0")
}

application {
    mainClass.set("com.triplify.ui.MainApp")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=javafx.graphics",
        "--enable-native-access=ALL-UNNAMED"
    )
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}
