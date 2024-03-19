plugins {
    id("java")
    `maven-publish`
}

group = "org.nickcoblentz.montoya.utilities"
version = "1.2"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("net.portswigger.burp.extensions:montoya-api:+")
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