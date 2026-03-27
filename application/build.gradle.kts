plugins {
    `java-library`
}

dependencies {
    api(project(":domain"))

    implementation("org.hibernate.validator:hibernate-validator:9.1.0.Final")
    implementation("org.glassfish.expressly:expressly:6.0.0")
}
