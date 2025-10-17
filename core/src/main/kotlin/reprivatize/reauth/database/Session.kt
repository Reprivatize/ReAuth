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

package reprivatize.reauth.database

import kotlinx.serialization.Serializable
import reprivatize.reauth.reAuthServer
import kotlin.time.Duration

@Serializable
data class Session(
    val internalId: Int,
    val hash: ByteArray,
    val salt: ByteArray,
    val createdAt: Long,
    val validFor: Duration = reAuthServer.config.session.duration(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        if (internalId != other.internalId) return false
        if (createdAt != other.createdAt) return false
        if (!hash.contentEquals(other.hash)) return false
        if (!salt.contentEquals(other.salt)) return false
        if (validFor != other.validFor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = internalId
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + hash.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + validFor.hashCode()
        return result
    }
}