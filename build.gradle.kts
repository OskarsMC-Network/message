import io.papermc.hangarpublishplugin.model.Platforms
import java.io.*

plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `maven-publish`
    id("xyz.jpenilla.run-velocity") version "2.0.0"
    id("io.papermc.hangar-publish-plugin") version "0.0.4"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.bstats:bstats-velocity:3.0.1")
    implementation("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
    implementation("cloud.commandframework:cloud-velocity:1.8.2")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.8.2")
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.0.0")
}

fun runCommand(command: String): String {
    return Runtime
            .getRuntime()
            .exec(command)
            .let { process ->
                process.waitFor()
                val output = process.inputStream.use {
                    it.bufferedReader().use(BufferedReader::readText)
                }
                process.destroy()
                output.trim()
            }
}

val release = System.getenv("GRADLE_RELEASE").equals("true", ignoreCase = true)
val gitHash = runCommand("git rev-parse --short HEAD")
group = "com.oskarsmc"
version = "1.5.0"

if (!release) {
    version = "$version-$gitHash-SNAPSHOT"
}


tasks {
    processResources {
        expand("project" to project)
    }

    shadowJar {
        dependencies {
            include {
                it.moduleGroup == "org.bstats" || it.moduleGroup == "cloud.commandframework" || it.moduleGroup == "io.leangen.geantyref"
            }
        }
        relocate("org.bstats", "com.oskarsmc.message.relocated.bstats")
        relocate("cloud.commandframework", "com.oskarsmc.message.relocated.cloud")
        relocate("io.leangen.geantyref", "com.oskarsmc.message.relocated.geantyref")
    }

    build {
        dependsOn(named("shadowJar"))
    }

    runVelocity {
        // Configure the Velocity version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        velocityVersion("3.1.2-SNAPSHOT")
    }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Implementation-Title"] = "message"
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Vendor"] = "OskarsMC"
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String?
            artifactId = project.name
            version = project.version as String?

            from(components["java"])
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://repository.oskarsmc.com/releases")
            val snapshotsRepoUrl = uri("https://repository.oskarsmc.com/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_SECRET")
            }
        }
    }
}

hangarPublish {
    if (!release) {
        publications.register("dev") {
            namespace("OskarsMC-Plugins", "message")
            channel.set("Development")
            apiKey.set(System.getenv("HANGAR_API_KEY"))
            version.set(project.version.toString())

            changelog.set(runCommand("git log -n 1"))


            platforms {
                register(Platforms.VELOCITY) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf("3.2"))
                }
            }
        }
    } else {
        publications.register("publish") {
            namespace("OskarsMC-Plugins", "message")
            channel.set("Release")
            apiKey.set(System.getenv("HANGAR_API_KEY"))
            version.set(project.version.toString())


            changelog.set(System.getenv("HANGAR_RELEASE_CHANGELOG"))

            platforms {
                register(Platforms.VELOCITY) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(listOf("3.2"))
                }
            }
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}
