import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
}

group = "com.minmusxminmus"
version = "1.0"

repositories {
    mavenCentral()
    maven {
        name = "m2-dv8tion"
        url = uri("https://m2.dv8tion.net/releases")
    }
}

dependencies {
    implementation(group = "net.dv8tion", name = "JDA", version = "4.3.0_277")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}