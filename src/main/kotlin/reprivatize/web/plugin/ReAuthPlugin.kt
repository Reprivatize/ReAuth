/*
 *     ReAuth-Backend: ReAuthPlugin.kt
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

package reprivatize.web.plugin

import io.ktor.http.*
import io.ktor.server.routing.*
import reprivatize.web.reAuthServer

abstract class ReAuthPlugin {
    fun routes(block: Route.() -> Unit) = apply { reAuthServer().pluginRoutes.add(block) }

    fun registerMethod(method: HttpMethod) = apply { reAuthServer().allowedMethods += method }
    fun registerMethods(vararg methods: HttpMethod) = apply { reAuthServer().allowedMethods.addAll(methods) }
    fun registerMethods(methods: List<HttpMethod>) = apply { reAuthServer().allowedMethods.addAll(methods) }
    fun registerHeader(header: String) = apply { reAuthServer().allowedHeaders += header }
    fun registerHeaders(vararg headers: String) = apply { reAuthServer().allowedHeaders.addAll(headers) }
    fun registerHeaders(headers: List<String>) = apply { reAuthServer().allowedHeaders.addAll(headers) }
    fun registerHost(host: String) = apply { reAuthServer().allowedHosts += host }
    fun registerHosts(vararg hosts: String) = apply { reAuthServer().allowedHosts.addAll(hosts) }
    fun registerHosts(hosts: List<String>) = apply { reAuthServer().allowedHosts.addAll(hosts) }

    abstract fun enable()
    abstract fun disable()
}