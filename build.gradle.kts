plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    //assertJ
    testImplementation("org.assertj:assertj-core:3.26.0")
    // Mockito для моков и верификации
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}