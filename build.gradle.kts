plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.nickcoblentz.montoya.utilities"
version = "1.4"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    //testImplementation(platform("org.junit:junit-bom:5.9.1"))
    //testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.portswigger.burp.extensions:montoya-api:+")
    implementation("com.nickcoblentz.montoya:MontoyaLibrary:0.1.5")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ncoblentz/BurpMontoyaRetryRequests")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GHUSERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GHTOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}