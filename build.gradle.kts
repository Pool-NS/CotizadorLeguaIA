plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.3.14" apply false
    id("com.google.gms.google-services") version "4.6.0" apply false
    id("com.google.firebase.crashlytics") version "3.0.8" apply false
    id("com.google.firebase.firebase-perf") version "2.0.2" apply false
    id("com.google.firebase.appdistribution") version "5.3.1" apply false
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
        property("sonar.exclusions", ".gradle/**")
        property("sonar.java.binaries", "app/build/tmp/kotlin-classes/debug")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "qa/coverage/jacoco.xml"
        )
    }
}
