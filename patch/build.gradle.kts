plugins {
    packetevents.`patching-conventions`
    `maven-publish`
}

dependencies {
    api(libs.adventure.text.serializer.gson)
    api(libs.adventure.text.serializer.json.legacy)
}

tasks {
    shadowJar {
        dependencies {
            exclude(dependency("net.kyori:adventure-api:.*"))
            exclude(dependency("net.kyori:adventure-key:.*"))
            exclude(dependency("net.kyori:adventure-nbt:.*"))
            exclude(dependency("net.kyori:examination-api:.*"))
            exclude(dependency("net.kyori:examination-string:.*"))
            exclude(dependency("com.google.code.gson:gson:.*"))
            exclude("META-INF/services/**")
        }
    }
}

subprojects {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
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