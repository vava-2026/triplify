dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    implementation("org.xerial:sqlite-jdbc:3.51.2.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
}
