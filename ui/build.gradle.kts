plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":application"))

    runtimeOnly("ch.qos.logback:logback-classic:1.5.32")

    // Icon library – Ikonli with Feather pack
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-feather-pack:12.4.0")
}

application {
    mainClass.set("com.triplify.ui.MainApp")
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}
