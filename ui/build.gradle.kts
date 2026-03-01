plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation(project(":infrastructure"))

    runtimeOnly("ch.qos.logback:logback-classic:1.5.32")

    // Icon library – Ikonli with Feather pack
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-feather-pack:12.4.0")

    implementation("com.google.inject:guice:7.0.0")
    implementation("net.sizovs:pipelinr:0.11")
}

application {
    mainClass.set("com.triplify.ui.MainApp")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=javafx.graphics"
    )
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}
