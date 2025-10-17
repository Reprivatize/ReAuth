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

package reprivatize.reauth.session

import kotlinx.serialization.Serializable
import reprivatize.reauth.reAuthServer
import reprivatize.reauth.serializer.UUIDSerializer
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class Session(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val macKey: ByteArray,
    val createdAt: Long,
    val validFor: Duration = reAuthServer.config.session.validForDuration(),
) {
    fun expired(): Boolean =
        Instant.fromEpochMilliseconds(createdAt) + validFor < Clock.System.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        if (createdAt != other.createdAt) return false
        if (uuid != other.uuid) return false
        if (!macKey.contentEquals(other.macKey)) return false
        if (validFor != other.validFor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createdAt.hashCode()
        result = 31 * result + uuid.hashCode()
        result = 31 * result + macKey.contentHashCode()
        result = 31 * result + validFor.hashCode()
        return result
    }
}