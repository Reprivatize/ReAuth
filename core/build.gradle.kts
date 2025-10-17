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

plugins {
    alias(libs.plugins.ktor)
}

group = "me.reprivatize.reauth"
version = "1.0.0"

application {
    mainClass = "reprivatize.reauth.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.database)
    implementation(libs.bundles.utilities)

    api(libs.pluggable)

    implementation(project(":plugin:api"))

    testApi(libs.ktor.server.test.host)
    testApi(libs.kotlin.test)
}

kotlin {
    jvmToolchain(21)
}