plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version("9.2.2")
}

val artifactId = "plugin-engine-bungee"
version = "1.0.0"

repositories {
    maven("https://hub.spigotmc.org/nexus/repository/public/")
}

dependencies {
    implementation(project(":common"))

    // Paper
    compileOnly("net.md-5:bungeecord-api:1.21-R0.5-SNAPSHOT")

    // Cloud Command Framework
    compileOnly("org.incendo:cloud-annotations:2.0.0")
    compileOnly("org.incendo:cloud-bungee:2.0.0-beta.10")

    // Moss
    compileOnly("games.negative.moss:moss-common:1.2.1")
    compileOnly("games.negative.moss:moss-bungeecord:1.2.1")

    // Spring & Jakarta
    compileOnly("org.springframework:spring-context:6.2.13")
    compileOnly("jakarta.annotation:jakarta.annotation-api:3.0.0")

    // Adventure
    compileOnly("net.kyori:adventure-platform-bungeecord:4.4.1")
    compileOnly("net.kyori:adventure-text-minimessage:4.26.1")

    // ConfigLib
    compileOnly("de.exlll:configlib-yaml:4.8.1")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveBaseName.set(artifactId)
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.shadowJar) {
                builtBy(tasks.shadowJar)
            }

            groupId = project.group.toString()
            artifactId = artifactId
            version = project.version.toString()

            pom {
                name.set(artifactId)
                description.set(project.description)
                url.set("https://github.com/negative-games/plugin-engine")

                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("ericlmao")
                        name.set("Eric")
                    }
                }
            }
        }
    }
}
