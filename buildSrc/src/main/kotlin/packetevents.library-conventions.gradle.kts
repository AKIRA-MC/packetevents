import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.Node

plugins {
    `java-library`
    `maven-publish`
}

group = rootProject.group
version = rootProject.version
description = rootProject.description

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
}

val isShadow = project.pluginManager.hasPlugin("com.gradleup.shadow")

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")
}

java {
    withSourcesJar()
    withJavadocJar()
    disableAutoTargetJvm()
}

tasks {
    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 8
    }

    javadoc {
        title = "packetevents-${project.name} v${rootProject.version}"
        options.encoding = Charsets.UTF_8.name()
        options.overview = rootProject.file("buildSrc/src/main/resources/javadoc-overview.html").toString()
        setDestinationDir(file("${project.layout.buildDirectory.asFile.get()}/docs/javadoc"))
        options {
            (this as CoreJavadocOptions).addBooleanOption("Xdoclint:none", true)
        }
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching(listOf("plugin.yml", "bungee.yml", "velocity-plugin.json", "fabric.mod.json")) {
            expand("version" to project.version)
        }
    }

    jar {
        archiveClassifier = "default"
    }

    defaultTasks("build")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "com.github.retrooper"
            artifactId = "packetevents-${project.name}"
            version = project.version.toString()

            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("https://repo.akiramc.fr/base/")
            credentials {
                username = findProperty("akiraBaseUsername")?.toString() ?: ""
                password = findProperty("akiraBasePassword")?.toString() ?: ""
            }
        }
    }
}


// So that SNAPSHOT is always the latest SNAPSHOT
configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
}

val taskNames = gradle.startParameter.taskNames
if (taskNames.any { it.contains("build") }
    && taskNames.any { it.contains("publish") }) {
    throw IllegalStateException("Cannot build and publish at the same time.")
}
