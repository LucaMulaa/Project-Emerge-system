import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.collektive)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.taskTree)
}

multiJvm {
    jvmVersionForCompilation.set(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.collektive)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.mktt)
    testImplementation(libs.bundles.kotlin.testing)
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(
            *TestLogEvent.entries.toTypedArray(),
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
