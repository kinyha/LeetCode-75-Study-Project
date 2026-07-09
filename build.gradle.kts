plugins {
    kotlin("jvm") version "2.4.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-context:7.0.8")

    testImplementation(kotlin("test"))
    //assertJ
    testImplementation("org.assertj:assertj-core:3.27.7")
    // Mockito для моков и верификации
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testImplementation("org.mockito:mockito-core:5.23.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
