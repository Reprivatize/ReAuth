/*
 *     ReAuth-Backend: build.gradle.kts
 *     Copyright (C) 2025 mtctx
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

val exposed_version: String by project
val postgres_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.0"
    id("io.ktor.plugin") version "3.3.0"
    id("org.jetbrains.dokka") version "2.1.0-Beta"
    id("org.jetbrains.dokka-javadoc") version "2.1.0-Beta"
}

group = "me.reprivatize.web"
version = "1.0.0"

application {
    mainClass = "reprivatize.web.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    api("io.ktor:ktor-server-compression")
    api("io.ktor:ktor-server-cors")
    api("io.ktor:ktor-server-hsts")
    api("io.ktor:ktor-server-http-redirect")
    api("io.ktor:ktor-server-core")
    api("io.ktor:ktor-server-content-negotiation")
    api("io.ktor:ktor-serialization-kotlinx-json")
    api("org.jetbrains.exposed:exposed-core:$exposed_version")
    api("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    api("org.postgresql:postgresql:$postgres_version")
    api("io.github.flaxoos:ktor-server-rate-limiting:2.2.1")
    api("io.ktor:ktor-server-cio")
    api("ch.qos.logback:logback-classic:$logback_version")
    api("org.bouncycastle:bcpkix-jdk18on:1.82")

    implementation("dev.mtctx.library:utilities:1.1.0")
    implementation("dev.mtctx.library:pluggable:1.0.0")

    implementation("org.jetbrains.dokka:dokka-gradle-plugin:2.1.0-Beta")

    testApi("io.ktor:ktor-server-test-host")
    testApi("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html").get().asFile)
    }
    dokkaPublications.javadoc {
        outputDirectory.set(layout.buildDirectory.dir("dokka/javadoc").get().asFile)
    }

    dokkaSourceSets.configureEach {
        jdkVersion.set(21)
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(uri("https://github.com/Reprivatize/ReAuth/tree/main/src/main/kotlin/reprivatize/web"))
            remoteLineSuffix.set("#L")
        }
    }
}

kotlin {
    jvmToolchain(21)
}