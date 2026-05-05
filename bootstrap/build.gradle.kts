plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.gradleup.shadow") version "9.4.1"
}

val javafxVersion = "25"
val javafxNativeModules = listOf("javafx-base", "javafx-controls", "javafx-fxml", "javafx-graphics")
val javafxPlatforms = listOf("win", "mac", "mac-aarch64", "linux", "linux-aarch64")

val nativeConfigs: List<Configuration> = javafxPlatforms.map { platform ->
    val cfgName = "javafxNatives_${platform.replace("-", "_")}"
    configurations.create(cfgName) {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = false
    }.also { cfg ->
        javafxNativeModules.forEach { module ->
            dependencies.add(cfgName, "org.openjfx:$module:$javafxVersion:$platform")
        }
    }
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":application"))
    implementation(project(":infrastructure"))

    runtimeOnly("ch.qos.logback:logback-classic:1.5.32")
}

application {
    mainClass.set("com.triplify.bootstrap.Launcher")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=javafx.graphics",
        "--enable-native-access=ALL-UNNAMED"
    )
}

javafx {
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.shadowJar {
    archiveBaseName.set("triplify")
    archiveClassifier.set("")
    archiveVersion.set("")
    configurations = listOf(project.configurations.runtimeClasspath.get()) + nativeConfigs
    manifest {
        attributes["Main-Class"] = "com.triplify.bootstrap.Launcher"
    }
    mergeServiceFiles()
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/*.MF")
}

tasks.register("buildRelease") {
    dependsOn(tasks.shadowJar)
    doLast {
        val outDir = layout.buildDirectory.dir("release").get().asFile
        outDir.mkdirs()

        val jar = tasks.shadowJar.get().archiveFile.get().asFile
        jar.copyTo(File(outDir, jar.name), overwrite = true)

        File(outDir, "run.bat").writeText(
            "@echo off\r\njava --enable-native-access=ALL-UNNAMED -jar \"${jar.name}\"\r\npause\r\n"
        )
        File(outDir, "run.sh").writeText(
            "#!/bin/sh\njava --enable-native-access=ALL-UNNAMED -jar \"${jar.name}\"\n"
        )

        println("Release output: ${outDir.absolutePath}")
    }
}
