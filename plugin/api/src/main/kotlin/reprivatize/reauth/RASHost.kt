/*
 * ReAuth-Backend (ReAuth-Backend.plugin.api.main): RASHost.kt
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

package reprivatize.reauth

import io.ktor.http.*
import io.ktor.server.routing.*

abstract class RASHost {
    abstract fun addPluginRoute(route: Route.() -> Unit)
    fun blacklistRouteForMiddleware(route: String) = blacklistRoutesForMiddleware(listOf(route))
    abstract fun blacklistRoutesForMiddleware(routes: List<String>)

    fun registerMethod(method: HttpMethod) = registerMethods(listOf(method))
    abstract fun registerMethods(methods: List<HttpMethod>)
    fun registerHeader(header: String) = registerHeaders(listOf(header))
    abstract fun registerHeaders(headers: List<String>)
    fun registerHost(host: String) = registerHosts(listOf(host))
    abstract fun registerHosts(hosts: List<String>)
}