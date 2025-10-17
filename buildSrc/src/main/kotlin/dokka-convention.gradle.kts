/*
 *     ReAuth-Backend: dokka-convention.gradle.kts
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