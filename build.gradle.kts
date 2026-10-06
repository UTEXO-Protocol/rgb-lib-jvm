import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Compression
import org.gradle.api.tasks.bundling.Tar
import java.net.URI

plugins {
    `java-library`
    `maven-publish`
    id("org.jreleaser") version "1.19.0"
}

group = providers.gradleProperty("GROUP_ID").orElse("com.utexo").get()
version = providers.gradleProperty("VERSION_NAME").orElse("0.0.0-dev").get()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
    withJavadocJar()
}

val nativeOutDir = layout.buildDirectory.dir("native/linux-x86_64")
val nativeBundleDir = layout.buildDirectory.dir("native-bundle")

tasks.register<Exec>("buildNativeLinuxX64") {
    group = "build"
    description = "Build rgb-lib C-FFI shared library for linux-x86_64 using submodule"
    commandLine("bash", "scripts/build_native_linux_x64.sh")
}

tasks.register<Tar>("packageNativeLinuxX64") {
    group = "build"
    description = "Package native linux-x86_64 artifacts (.so + headers)"
    dependsOn("buildNativeLinuxX64")
    destinationDirectory.set(nativeBundleDir)
    archiveFileName.set("rgb-lib-jvm-linux-x86_64-${project.version}.tar.gz")
    compression = Compression.GZIP
    from(nativeOutDir)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn("buildNativeLinuxX64")
    from(nativeOutDir) {
        into("native/linux-x86_64")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.named("packageNativeLinuxX64")) {
                classifier = "linux-x86_64-native"
                extension = "tar.gz"
            }
            groupId = providers.gradleProperty("GROUP_ID").orElse(group.toString()).get()
            artifactId = providers.gradleProperty("ARTIFACT_ID").orElse("rgb-lib-jvm").get()
            version = providers.gradleProperty("VERSION_NAME").orElse(version.toString()).get()

            pom {
                name.set("rgb-lib-jvm")
                description.set("JVM wrapper and native bundle packaging for rgb-lib on Linux")
                url.set("https://github.com/UTEXO-Protocol/rgb-lib-jvm")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("utexo")
                        name.set("UTEXO")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/UTEXO-Protocol/rgb-lib-jvm.git")
                    developerConnection.set("scm:git:ssh://git@github.com/UTEXO-Protocol/rgb-lib-jvm.git")
                    url.set("https://github.com/UTEXO-Protocol/rgb-lib-jvm")
                }
            }
        }
    }

    repositories {
        maven {
            name = "stagingDeploy"
            url = URI(layout.buildDirectory.dir("staging-deploy").get().asFile.toURI().toString())
        }

        if (providers.gradleProperty("publishGithubPackages").orElse("false").get() == "true") {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/UTEXO-Protocol/rgb-lib-jvm")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
    }
}

jreleaser {
    project {
        name.set("rgb-lib-jvm")
        description.set("JVM distribution for rgb-lib Linux C-FFI integration")
        longDescription.set("JVM wrapper and native Linux artifacts built from rgb-lib submodule")
        website.set("https://github.com/UTEXO-Protocol/rgb-lib-jvm")
        authors.set(listOf("UTEXO"))
        license.set("MIT")
        licenseUrl.set("https://spdx.org/licenses/MIT.html")

        java {
            groupId.set(providers.gradleProperty("GROUP_ID").orElse("com.utexo"))
            version.set("17")
        }
    }

    release {
        github {
            enabled.set(true)
        }
    }

    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
        mode.set(org.jreleaser.model.Signing.Mode.COMMAND)
        command {
            keyName.set(providers.gradleProperty("jreleaser.gpg.keyName"))
            passphrase.set(providers.gradleProperty("jreleaser.gpg.passphrase"))
        }
    }

    deploy {
        maven {
            pomchecker {
                failOnError.set(false)
                failOnWarning.set(false)
            }

            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("build/staging-deploy")
                    verifyPom.set(false)
                }
            }
        }
    }
}
