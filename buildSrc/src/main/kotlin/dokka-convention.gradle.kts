/*
 * ReAuth-Backend (ReAuth-Backend.buildSrc.main): dokka-convention.gradle.kts
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
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
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
            localDirectory.set(file("${project.name}/src/main/kotlin"))
            remoteUrl.set(uri("https://github.com/Reprivatize/ReAuth-Backend/tree/main/${project.name}/src/main/kotlin/reprivatize/reauth/"))
            remoteLineSuffix.set("#L")
        }
    }
}