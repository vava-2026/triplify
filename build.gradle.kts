group = "com.triplify"
version = "1.0-SNAPSHOT"

subprojects {
    plugins.apply("java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        // Logging
        "implementation"("org.slf4j:slf4j-api:2.0.17")

        "implementation"("com.google.inject:guice:7.0.0")

        // Tests
        "testImplementation"(platform("org.junit:junit-bom:5.10.0"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
