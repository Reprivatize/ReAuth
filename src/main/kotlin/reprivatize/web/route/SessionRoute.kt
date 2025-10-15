/*
 *     ReAuth-Backend: SessionRoute.kt
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

package reprivatize.web.route

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import reprivatize.web.bodies.SessionIdOnlyBody
import reprivatize.web.isInternalSecretBased
import reprivatize.web.reAuthServer
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Route.session() {
    route("/session") {
        post("/create") {
            if (!call.isInternalSecretBased()) return@post call.respond(HttpStatusCode.Forbidden)
            call.respond(HttpStatusCode.Created, reAuthServer().sessionService.create())
        }

        post("/valid") {
            val sessionIdOnlyBody = call.receive<SessionIdOnlyBody>()
            val (internalId, sessionId) = sessionIdOnlyBody.sessionId.split(":", limit = 2)
            call.respond(
                HttpStatusCode.OK,
                reAuthServer().sessionService.isValid(internalId, sessionId)
            )
        }

        delete("/invalidate") {
            val sessionIdOnlyBody = call.receive<SessionIdOnlyBody>()
            val internalId = sessionIdOnlyBody.sessionId.split(":", limit = 2)[0].toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            reAuthServer().sessionService.delete(internalId)
            call.respond(HttpStatusCode.OK)
        }
    }
}