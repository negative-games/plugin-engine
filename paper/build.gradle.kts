plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version("9.2.2")
}

val artifactId = "plugin-engine-paper"
version = "1.2.0"

dependencies {
    implementation(project(":common"))

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    // Gson
    compileOnly("com.google.code.gson:gson:2.11.0")
    testImplementation("com.google.code.gson:gson:2.11.0")

    // Cloud Command Framework
    compileOnly("org.incendo:cloud-annotations:2.0.0")
    compileOnly("org.incendo:cloud-paper:2.0.0-beta.10")

    // Moss
    compileOnly("games.negative.moss:moss-common:1.2.1")
    compileOnly("games.negative.moss:moss-paper:1.2.1")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.7")

    // PacketEvents
    compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")

    // Spring & Jakarta
    compileOnly("org.springframework:spring-context:6.2.13")
    compileOnly("jakarta.annotation:jakarta.annotation-api:3.0.0")

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
