/*
 *     ReAuth-Backend: Session.kt
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

package reprivatize.reauth.crypto

import mtctx.utilities.crypto.SECURE_RANDOM

fun generateSessionId(length: Int = 25): String {
    val allowedCharacters = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf(
        "-", "_", ".", ",", ";", ":", "#", "*", "+", "~", "!", "§", "$",
        "%", "&", "(", ")", "[", "]", "{", "}", "<", ">", "?", "="
    )
    return "REAUTH:${
        (1..length).map { allowedCharacters[SECURE_RANDOM.nextInt(allowedCharacters.size)] }.joinToString("")
    }"
}