plugins {
    id("java")
    id("application")
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("software.amazon.aurora.AuroraConnectionApp")
}

dependencies {
    implementation("software.amazon.jdbc:aws-advanced-jdbc-wrapper:2.6.3")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
