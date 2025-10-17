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

package reprivatize.reauth

import io.ktor.http.*
import io.ktor.server.routing.*

abstract class ReAuthPlugin {
    protected lateinit var server: ReAuthServer

    fun initialize(server: ReAuthServer) {
        this.server = server
    }

    fun routes(block: Route.() -> Unit) = apply { server.addPluginRoute(block) }

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

