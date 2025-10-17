/*
 * ReAuth-Backend (ReAuth-Backend.core.main): SessionRoute.kt
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

package reprivatize.reauth.session

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import reprivatize.reauth.cachedSessions
import reprivatize.reauth.isInternalSecretBased
import reprivatize.reauth.reAuthServer
import java.util.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Route.session() {
    route("/session") {
        post("/create") {
            if (!call.isInternalSecretBased()) return@post call.respond(
                HttpStatusCode.Forbidden,
                "This endpoint is for internal use only."
            )
            call.respond(HttpStatusCode.Created, reAuthServer.sessionService.create())
        }

        get("/valid") {
            val uuid = call.attributes[SessionAttributes.UUID]
            val macTag = call.attributes[SessionAttributes.MAC_TAG]

            call.respond(
                HttpStatusCode.OK,
                reAuthServer.sessionService.isValid(uuid, macTag)
            )
        }

        get("/expired") {
            val uuid = call.attributes[SessionAttributes.UUID]
            call.attributes[SessionAttributes.MAC_TAG]

            call.respond(
                HttpStatusCode.OK,
                reAuthServer.sessionService.isExpired(uuid)
            )
        }

        delete("/invalidate") {
            reAuthServer.sessionService.delete(call.attributes[SessionAttributes.UUID])
            call.respond(HttpStatusCode.OK)
        }
    }
}

val SessionCheckMiddleware = createApplicationPlugin("SessionCheckMiddleware") {
    onCall { call ->
        if (!reAuthServer.blacklistedRoutesForMiddleware.contains(call.request.path())) {
            val session = call.request.header(HttpHeaders.Authorization) ?: return@onCall call.respond(
                HttpStatusCode.Unauthorized,
                "Missing session"
            )

            val (uuid, macTag) = try {
                val parts = session.split(".", limit = 2)
                UUID.fromString(parts[0]) to parts[1].decodeBase64Bytes()
            } catch (_: Exception) {
                return@onCall call.respond(HttpStatusCode.BadRequest, "Invalid session format")
            }

            if (call.request.path() != "/reauth/session/valid" && call.request.path() != "/reauth/session/expired") {
                if (!reAuthServer.sessionService.isValid(uuid, macTag)) {
                    if (cachedSessions[uuid] != null && reAuthServer.sessionService.isExpired(uuid))
                        reAuthServer.sessionService.delete(uuid)

                    return@onCall call.respond(HttpStatusCode.Unauthorized, "Invalid session")
                }
            }

            call.attributes.put(SessionAttributes.UUID, uuid)
            call.attributes.put(SessionAttributes.MAC_TAG, macTag)
        }
    }
}