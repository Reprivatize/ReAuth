/*
 * ReAuth-Backend (ReAuth-Backend.plugin.auth.password.main): PasswordPlugin.kt
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

import io.ktor.server.routing.*

class PasswordPlugin : ReAuthPlugin() {
    override fun enable() {
        logger.info("Enabling PasswordPlugin..")

        logger.debug("Adding routes to middleware blacklist..")
        blacklistRoutesForMiddleware("login/password", "register/password")

        logger.debug("Adding routes...")
        routes {
            get("/login/password") {

            }
            post("/register/password") {

            }
        }

        logger.info("Enabled PasswordPlugin!")
    }

    override fun disable() {
        TODO("Not yet implemented")
    }
}