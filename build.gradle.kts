plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.jiandong"
version = "0.0.1-SNAPSHOT"
description = "legendary-integration"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.integration:spring-integration-file")  // file
    implementation("org.springframework.integration:spring-integration-sftp")  // sftp
    implementation("org.springframework.boot:spring-boot-starter-mail")        // boot-mail
    implementation("org.springframework.integration:spring-integration-mail")  // mail
    implementation("org.apache.commons:commons-email2-jakarta:2.0.0-M1")       // mail-parser
    implementation("org.springframework.boot:spring-boot-starter-data-redis")  // boot-redis
    implementation("org.springframework.integration:spring-integration-redis") // redis
    implementation("org.springframework.integration:spring-integration-http")  // http
    implementation("org.springframework.integration:spring-integration-event") // event
    implementation("org.springframework.boot:spring-boot-starter-activemq")    // boot-activemq
    implementation("org.apache.activemq:activemq-broker")                      // activemq-broker
    implementation("org.springframework.integration:spring-integration-jms")   // jms
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.integration:spring-integration-test")
    testImplementation("org.testcontainers:testcontainers-activemq")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("com.icegreen:greenmail:2.1.9")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {

    testLogging.showStandardStreams = true

    useJUnitPlatform()

    finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}

tasks.withType<JacocoReportBase> {
    classDirectories.setFrom(
        classDirectories.files.map { file ->
            fileTree(file) {
                exclude(
                    "**/com/jiandong/proto/**",
                )
            }
        }
    )
}

tasks.jacocoTestReport {
    reports {
        xml.required = false
        csv.required = true
        html.required = false
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = BigDecimal(0.15)
            }
        }
    }
}