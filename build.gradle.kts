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

        // Tests
        "testImplementation"(platform("org.junit:junit-bom:5.10.0"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")

        // Lombok
        "compileOnly"("org.projectlombok:lombok:1.18.44")
        "annotationProcessor"("org.projectlombok:lombok:1.18.44")

        "testCompileOnly"("org.projectlombok:lombok:1.18.44")
        "testAnnotationProcessor"("org.projectlombok:lombok:1.18.44")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
