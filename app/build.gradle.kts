/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.6/userguide/building_java_projects.html
 */

import java.io.OutputStream

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.10"

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}



data class ArtifactMeta(
        val group: String,
        val name: String,
        val version: String? = null,
        val configuration: String? = null,
        val classifier: String? = null,
        val ext: String? = null,
)

val artifactsToDownload: List<ArtifactMeta> = listOf(
        ArtifactMeta(group="org.jetbrains.kotlin",name="kotlin-test-junit5"),
        ArtifactMeta(group="org.junit.jupiter",name="junit-jupiter-engine", version="5.9.1"),
        ArtifactMeta(group="com.google.guava",name="guava", version="31.1-jre")

)

dependencies {

    for (artifact in artifactsToDownload) {
        implementation(
                group=artifact.group,
                name=artifact.name,
                version=artifact.version,
                configuration=artifact.configuration,
                classifier=artifact.classifier,
                ext=artifact.ext
        )
    }
}

application {
    // Define the main class for the application.
    mainClass.set("artifacts.downloader.AppKt")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}


val archiveRootDirectoryConvention = "repository"

val projectMaven = "${project.buildDir}/${archiveRootDirectoryConvention}"
val gradleCache = "${gradle.gradleUserHomeDir}/caches"
val gradleArtifacts = "$gradleCache/modules-2/files-2.1"

tasks.register<Sync>("syncToLocalMaven") {

    dependsOn("clean")
    dependsOn("run")

    from(gradleArtifacts)

    into(projectMaven)

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    eachFile {
        val parts = this.path.split("/").toMutableList()

        // Fix org.jetbrains.kotlin/kotlin-script/1.5.0/<gradle-hash-for-caching>/<.pom>
        //     -> org/jetbrains/kotlin/kotlin-script/1.5.0/...
        parts[0] = parts[0].replace(".", "/")

        // Remove the gradle hashing part
        parts.removeAt(index=parts.lastIndex-1)
        this.path = parts.joinToString(separator="/")
    }

    includeEmptyDirs = false
}

val outputPackge: String = artifactsToDownload.joinToString(separator="_") {
    "${it.name}_${it.version ?: "latest"}"
}

tasks.register<Tar>("packageLocalRepository") {
    dependsOn("syncToLocalMaven")

    from(projectMaven)
    into(archiveRootDirectoryConvention)
    compression = Compression.GZIP

    extension = "tar.gz"
    baseName = outputPackge
}


fun outputStreamOf(path: String): OutputStream {
    val f = File(path)
    f.createNewFile()
    return f.outputStream()
}


tasks.register<Exec>("encodeToBase64") {
    dependsOn("packageLocalRepository")

    val pkgTask = tasks.getByPath("packageLocalRepository") as Tar
    val targzPath = pkgTask.archiveFile.get().asFile.absolutePath

    standardOutput = outputStreamOf(path="$targzPath.txt")
    commandLine = listOf("base64", targzPath)
}
