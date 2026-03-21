plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

dependencies {
    implementation(project(":application"))

    // Icon library – Ikonli with Feather pack
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-feather-pack:12.4.0")

    // Routing
    implementation("com.github.rahulstech:javafx-routing:2.0.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}
