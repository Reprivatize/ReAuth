/*
 *     ReAuth-Backend: SessionService.kt
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

import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import reprivatize.reauth.cacheSession
import reprivatize.reauth.cachedSessions
import reprivatize.reauth.crypto.HmacSha256
import reprivatize.reauth.reAuthServer
import reprivatize.reauth.uncacheSession
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SessionService(database: Database) {
    object Sessions : Table() {
        val uuid = uuid("uuid").uniqueIndex()
        val macKey = binary("macKey")
        val createdAt = long("createdAt")
        val validFor = long("validFor")

        override val primaryKey = PrimaryKey(uuid)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Sessions)
        }
    }

    @ExperimentalTime
    suspend fun create(): String = dbQuery {
        val sessionUUID = UUID.randomUUID()
        val randomMacKey = HmacSha256.generateKey()
        val mac = HmacSha256.generate(sessionUUID.toString(), randomMacKey)

        Sessions.insert {
            it[uuid] = sessionUUID
            it[macKey] = randomMacKey
            it[createdAt] = Clock.System.now().toEpochMilliseconds()
            it[validFor] = reAuthServer.config.session.validForDuration().inWholeMilliseconds
        }

        return@dbQuery "$sessionUUID.${mac.tag.encodeBase64()}"
    }

    suspend fun create(session: Session) = dbQuery {
        Sessions.insert {
            it[uuid] = session.uuid
            it[macKey] = session.macKey
            it[createdAt] = session.createdAt
            it[validFor] = session.validFor.inWholeMilliseconds
        }
    }

    suspend fun read(uuid: UUID): Session? = dbQuery {
        Sessions.selectAll()
            .where { Sessions.uuid eq uuid }
            .map {
                Session(
                    it[Sessions.uuid],
                    it[Sessions.macKey],
                    it[Sessions.createdAt],
                    it[Sessions.validFor].milliseconds,
                )
            }
            .singleOrNull()
    }

    suspend fun delete(uuid: UUID) {
        dbQuery {
            Sessions.deleteWhere { Sessions.uuid eq uuid }
            uncacheSession(uuid)
        }
    }

    suspend fun isValid(uuid: UUID, clientMacTagBase64: String): Boolean {
        return try {
            isValid(uuid, clientMacTagBase64.decodeBase64Bytes())
        } catch (e: Exception) {
            reAuthServer.logger.error("Failed to decode client mac tag: $clientMacTagBase64", e)
            false
        }
    }

    suspend fun isValid(uuid: UUID, clientMacTag: ByteArray): Boolean = dbQuery {
        val session = session(uuid) ?: return@dbQuery false
        if (!HmacSha256.verify(uuid.toString(), clientMacTag, session.macKey)) return@dbQuery false
        return@dbQuery !session.expired()
    }

    suspend fun isExpired(uuid: UUID): Boolean = dbQuery {
        val session = session(uuid) ?: return@dbQuery true
        return@dbQuery session.expired()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun session(uuid: UUID): Session? =
        dbQuery { cachedSessions[uuid] ?: read(uuid)?.apply { cacheSession(this) } }
}