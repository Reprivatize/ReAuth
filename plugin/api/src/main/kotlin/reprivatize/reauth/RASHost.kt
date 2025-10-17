/*
 *     ReAuth-Backend: RASHost.kt
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