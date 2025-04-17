plugins {
    id("java")
}

group = "com.upgrades"
version = "2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    project.version="$version"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    manifest {
        attributes("Main-Class" to "com.upgrade.tools.SchemeConverterMain")
    }
}

tasks.test {
    useJUnitPlatform()
}