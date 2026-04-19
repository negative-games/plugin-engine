import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java")
    id("maven-publish")
}

subprojects {
    apply(plugin = "java")

    group = "games.negative.engine"

    repositories {
        mavenCentral()

        // Negative Games
        maven("https://repo.negative.games/repository/maven-releases/")
        maven("https://repo.negative.games/repository/maven-snapshots/")

        // CodeMC
        maven("https://repo.codemc.io/repository/maven-releases/")
        maven("https://repo.codemc.io/repository/maven-snapshots/")

        // PaperMC
        maven("https://repo.papermc.io/repository/maven-public/")

        // HelpChat
        maven("https://repo.helpch.at/releases")
    }

    dependencies {
        add("testImplementation", platform("org.junit:junit-bom:5.12.2"))
        add("testImplementation", "org.junit.jupiter:junit-jupiter")
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

tasks.withType<Jar>() {
    enabled = false
}


subprojects {
    plugins.withId("maven-publish") {
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "nexus"

                    val snapshotsUrl = findProperty("nexusSnapshotsUrl") as String? ?: "https://repo.negative.games/repository/maven-snapshots"
                    val releasesUrl  = findProperty("nexusReleasesUrl")  as String? ?: "https://repo.negative.games/repository/maven-releases"

                    val isRelease = (findProperty("isRelease") == "true")
                    url = uri(if (isRelease) releasesUrl else snapshotsUrl)

                    credentials {
                        username = findProperty("nexusUsername") as String?
                        password = findProperty("nexusPassword") as String?
                    }
                }
            }
        }
    }
}
