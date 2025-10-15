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

package reprivatize.web.database

import kotlinx.coroutines.Dispatchers
import mtctx.utilities.crypto.Argon2
import mtctx.utilities.crypto.secureEquals
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import reprivatize.web.crypto.generateSessionId
import reprivatize.web.reAuthServer
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class SessionService(database: Database) {
    object Sessions : Table() {
        val internalId = integer("id").autoIncrement()
        val hash = binary("hash", 64)
        val salt = binary("salt", 16)
        val createdAt = long("createdAt")
        val validFor = long("validFor")

        override val primaryKey = PrimaryKey(internalId)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Sessions)
        }
    }

    @ExperimentalTime
    suspend fun create(): String = dbQuery {
        val sessionId = generateSessionId()
        val hashResult = Argon2.hash(sessionId, reAuthServer().config.session.salt())
        val sessionInternalId = Sessions.insert {
            it[hash] = hashResult.hash
            it[salt] = hashResult.salt
            it[createdAt] = Clock.System.now().toEpochMilliseconds()
            it[validFor] = 30.minutes.inWholeMilliseconds
        }[Sessions.internalId]

        return@dbQuery "$sessionInternalId:$sessionId"
    }

    suspend fun create(session: Session) = dbQuery {
        Sessions.insert {
            it[hash] = session.hash
            it[salt] = session.salt
            it[createdAt] = session.createdAt
            it[validFor] = session.validFor.inWholeMilliseconds
        }[Sessions.internalId]
    }

    suspend fun read(hash: ByteArray): Session? = dbQuery {
        Sessions.selectAll()
            .where { Sessions.hash eq hash }
            .map {
                Session(
                    it[Sessions.internalId],
                    it[Sessions.hash],
                    it[Sessions.salt],
                    it[Sessions.createdAt],
                    it[Sessions.validFor].milliseconds,
                )
            }
            .singleOrNull()
    }

    suspend fun read(internalId: Int): Session? = dbQuery {
        Sessions.selectAll()
            .where { Sessions.internalId eq internalId }
            .map {
                Session(
                    it[Sessions.internalId],
                    it[Sessions.hash],
                    it[Sessions.salt],
                    it[Sessions.createdAt],
                    it[Sessions.validFor].milliseconds,
                )
            }
            .singleOrNull()
    }

    suspend fun delete(hash: ByteArray) {
        dbQuery {
            Sessions.deleteWhere { Sessions.hash eq hash }
        }
    }

    suspend fun delete(internalId: Int) {
        dbQuery {
            Sessions.deleteWhere { Sessions.internalId eq internalId }
        }
    }

    fun isValid(session: Session): Boolean =
        Instant.fromEpochMilliseconds(session.createdAt) + session.validFor > Clock.System.now()

    suspend fun isValid(internalId: String, sessionId: String): Boolean = dbQuery {
        if (internalId.toIntOrNull() == null) return@dbQuery false
        val session = Sessions.selectAll().where { Sessions.internalId eq internalId.toInt() }.map {
            Session(
                it[Sessions.internalId],
                it[Sessions.hash],
                it[Sessions.salt],
                it[Sessions.createdAt],
                it[Sessions.validFor].milliseconds,
            )
        }.singleOrNull()
        if (session == null) return@dbQuery false
        val hash = Argon2.hash(sessionId, session.salt).hash
        return@dbQuery hash secureEquals session.hash && Instant.fromEpochMilliseconds(session.createdAt) + session.validFor > Clock.System.now()

    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

