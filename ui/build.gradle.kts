plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":application"))

    runtimeOnly("ch.qos.logback:logback-classic:1.5.32")
}

application {
    mainClass.set("com.triplify.ui.MainApp")
}

javafx {
    version = "25"
    modules = listOf("javafx.controls")
}
