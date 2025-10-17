/*
 *     ReAuth-Backend: PasswordPlugin.kt
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

import io.ktor.server.routing.*

class PasswordPlugin : ReAuthPlugin() {
    override fun enable() {
        // Dont check for the session here
        blacklistRoutesForMiddleware("login/password", "register/password")

        routes {
            get("/login/password") {

            }
            post("/register/password") {

            }
        }
    }

    override fun disable() {
        TODO("Not yet implemented")
    }
}