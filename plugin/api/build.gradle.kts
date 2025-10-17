/*
 * ReAuth-Backend (ReAuth-Backend.plugin.api): build.gradle.kts
 * Copyright (C) 2025 mtctx
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the **GNU General Public License** as published
 * by the Free Software Foundation, either **version 3** of the License, or
 * (at your option) any later version.
 *
 * *This program is distributed WITHOUT ANY WARRANTY;** see the
 * GNU General Public License for more details, which you should have
 * received with this program.
 *
 * SPDX-FileCopyrightText: 2025 mtctx
 * SPDX-License-Identifier: GPL-3.0-only
 */

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    alias(libs.plugins.ktor)
    alias(libs.plugins.maven.publish)
    signing
}

group = "me.reprivatize.reauth"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.bundles.ktor.server) {
        exclude(group = "ch.qos.logback")
    }
    api(libs.bundles.database)
    api(libs.bundles.utilities)

    api("dev.mtctx.library:utilities:1.1.0")
}

mavenPublishing {
    coordinates(group.toString(), "reauth-plugin-api", version.toString())

    pom {
        name.set("ReAuth-Backend Plugin API")
        description.set("ReAuth-Backend's API for Plugins")
        inceptionYear.set("2025")
        url.set("https://github.com/Reprivatize/ReAuth-Backend/tree/main/plugin/api/src/main/kotlin")

        licenses {
            license {
                name.set("GNU General Public License v3.0")
                url.set("https://www.gnu.org/licenses/gpl-3.0.html")
                distribution.set("repo")
            }
        }

        scm {
            url.set("https://github.com/Reprivatize/ReAuth-Backend/tree/main/plugin/api/src/main/kotlin")
            connection.set("scm:git:git@github.com:Reprivatize/ReAuth-Backend.git")
            developerConnection.set("scm:git:ssh://git@github.com:Reprivatize/ReAuth-Backend.git")
        }

        developers {
            developer {
                id.set("mtctx")
                name.set("mtctx")
                email.set("me@mtctx.dev")
            }
        }

    }

    configure(KotlinJvm(JavadocJar.Dokka("dokkaGenerateJavadoc"), sourcesJar = true))

    signAllPublications()
    publishToMavenCentral(automaticRelease = true)
}

signing {
    useGpgCmd()
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}