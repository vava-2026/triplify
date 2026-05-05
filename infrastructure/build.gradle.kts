dependencies {
    implementation(project(":domain"))
    implementation("com.google.code.gson:gson:2.11.0") // Добавьте эту строку

    implementation("com.zaxxer:HikariCP:7.0.2")
    implementation("org.xerial:sqlite-jdbc:3.51.2.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
}