/*
 * ReAuth-Backend (ReAuth-Backend.plugin.api.main): ReAuthPlugin.kt
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
import org.slf4j.Logger
import reprivatize.reauth.service.RASSessionService

abstract class ReAuthPlugin {
    protected lateinit var server: RASHost
    protected lateinit var logger: Logger

    protected lateinit var sessionService: RASSessionService

    fun initialize(server: RASHost, logger: Logger, sessionService: RASSessionService) {
        this.server = server
        this.logger = logger
        this.sessionService = sessionService
    }

    fun routes(block: Route.() -> Unit) = apply { server.addPluginRoute(block) }
    fun blacklistRouteForMiddleware(route: String) = apply { server.blacklistRouteForMiddleware(route) }
    fun blacklistRoutesForMiddleware(routes: List<String>) = apply { server.blacklistRoutesForMiddleware(routes) }
    fun blacklistRoutesForMiddleware(vararg routes: String) =
        apply { server.blacklistRoutesForMiddleware(routes.toList()) }

    fun registerMethod(method: HttpMethod) = apply { server.registerMethod(method) }
    fun registerMethods(vararg methods: HttpMethod) = apply { server.registerMethods(methods.toList()) }
    fun registerMethods(methods: List<HttpMethod>) = apply { server.registerMethods(methods) }
    fun registerHeader(header: String) = apply { server.registerHeader(header) }
    fun registerHeaders(vararg headers: String) = apply { server.registerHeaders(headers.toList()) }
    fun registerHeaders(headers: List<String>) = apply { server.registerHeaders(headers) }
    fun registerHost(host: String) = apply { server.registerHost(host) }
    fun registerHosts(vararg hosts: String) = apply { server.registerHosts(hosts.toList()) }
    fun registerHosts(hosts: List<String>) = apply { server.registerHosts(hosts) }

    abstract fun enable()
    abstract fun disable()
}

