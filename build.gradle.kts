/*
 * ReAuth-Backend: build.gradle.kts
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

plugins {
    `dokka-convention`
}


allprojects {
    plugins.apply("dokka-convention")

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/dokka/maven")
    }

    configurations.all {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
}

subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    dokka(project(":core"))
    dokka(project(":plugin:api"))
    dokka(project(":plugin:auth:password"))
}

dokka {
    dokkaPublications.html {
        outputDirectory.set(layout.projectDirectory.dir("docs/html").asFile)
    }
    dokkaPublications.javadoc {
        outputDirectory.set(layout.projectDirectory.dir("docs/javadoc").asFile)
    }
}