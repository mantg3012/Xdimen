import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    java
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.kotlinJVM)
    alias(libs.plugins.ktlint)
}

group = "io.github.islamkhsh"
// JitPack passes the tag/commit via the VERSION env var; fall back to 0.0.8 for local builds.
version = System.getenv("VERSION") ?: "0.0.8"

// Pin Java + Kotlin to the same JVM target so the build is consistent
// regardless of the host JDK (e.g. JDK 25 here).
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

gradlePlugin {
    plugins {
        @Suppress("Detekt:UnusedPrivateMember")
        val plugin by creating {
            id = "io.github.islamkhsh.xdimen"
            displayName = "Xdimen"
            description = "Easily support android multiple screen sizes"
            implementationClass = "XdimenPlugin"
            tags = listOf("android", "dimensions", "tablet", "multiple-screens")
        }
    }
}

gradlePlugin {
    website = "https://islamkhsh.github.io/Xdimen/"
    vcsUrl = "https://github.com/IslamKhSh/xdimen"
}

repositories {
    google()
    mavenCentral()
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        @Suppress("Detekt:UnusedPrivateMember")
        val functionalTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(libs.jupiterApi)
                implementation(libs.jupiterEngine)
            }

            targets {
                all {
                    testTask.configure {
                        dependsOn(tasks.named("pluginUnderTestMetadata"))
                        classpath += files(tasks.named("pluginUnderTestMetadata"))
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(libs.kotlinStd)
    implementation(gradleApi())
    implementation(libs.xmlBuilder)

    testImplementation(libs.jupiterApi)
    testRuntimeOnly(libs.jupiterEngine)
    testImplementation(libs.mockk)
    "functionalTestImplementation"(gradleTestKit())
}

ktlint {
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
    reporters { reporter(ReporterType.HTML) }
    filter { exclude { it.file.path.contains("generated/") } }
}

@Suppress("UnstableApiUsage")
tasks.named("check") {
    dependsOn(testing.suites.named("functionalTest"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
