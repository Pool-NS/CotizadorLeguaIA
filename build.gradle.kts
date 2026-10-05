plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.sonarqube") version "7.5.0.8588"
}

sonar {
    properties {
        property("sonar.projectKey", "leguia-cotizador-android")
        property("sonar.projectName", "Cotizador Leguia Android")
        property(
            "sonar.host.url",
            providers.environmentVariable("SONAR_HOST_URL").orElse("http://localhost:9000").get()
        )
        property("sonar.sources", "app/src/main/java")
        property("sonar.tests", "app/src/test/java")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.java.binaries", "app/build/tmp/kotlin-classes/debug")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "app/build/reports/jacoco/testDebugUnitTestCoverage/testDebugUnitTestCoverage.xml"
        )
    }
}
