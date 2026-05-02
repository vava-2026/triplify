plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":application"))

    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-feather-pack:12.4.0")
    implementation("com.gluonhq:maps:2.0.0-ea+6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.4")

    implementation("com.github.rahulstech:javafx-routing:2.0.0")
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}
