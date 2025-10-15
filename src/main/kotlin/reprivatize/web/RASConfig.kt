/*
 *     ReAuth-Backend: RASConfig.kt
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

package reprivatize.web

import kotlinx.serialization.Serializable
import mtctx.utilities.crypto.Argon2.Companion.generateSalt
import mtctx.utilities.jsonForHumans
import mtctx.utilities.jsonForMachines
import mtctx.utilities.readAndDeserialize
import mtctx.utilities.serializeAndWrite
import okio.Path
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Serializable
data class RASConfig(
    val database: Database,
    val session: Session,
    val cors: CORS,
    val tsl: TSL,
    val port: Int = 8080,
    val internalHostsSecretKey: String,
    val enableCompression: Boolean = true,
) {
    @Serializable
    data class Database(
        val host: String,
        val port: Int,
        val database: String,
        val username: String,
        val password: String,
    )

    @Serializable
    data class Session(
        val durationTime: Double,
        val durationUnit: DurationUnit,
        val sessionLength: Int = 25,
        val argon2salt: String? = SESSION_DEFAULT_SALT_STRING,
    ) {
        fun duration() = durationTime.toDuration(durationUnit)
        fun salt() =
            if (argon2salt == null || argon2salt == SESSION_DEFAULT_SALT_STRING) generateSalt() else argon2salt.encodeToByteArray()
    }

    @Serializable
    data class CORS(
        val allowedMethods: MutableSet<RASHttpMethods> = mutableSetOf(RASHttpMethods.Delete, RASHttpMethods.Post),
        val allowedHeaders: MutableSet<RASHttpHeaders> = mutableSetOf(
            RASHttpHeaders.ContentType,
            RASHttpHeaders.Authorization
        ),
        val allowedHosts: MutableSet<String> = mutableSetOf("localhost"),
    )

    @Serializable
    data class TSL(
        val hstsIncludeSubDomains: Boolean = true,
        val sslPort: Int = 443,
        val permanentRedirect: Boolean = true,
    )

    fun save(path: Path = PATH) =
        this.serializeAndWrite(serializer(), path, false, atomicMove = true, jsonForHumans)

    companion object {
        val PATH = RUNNING_DIR.resolve("config.json")
        const val SESSION_DEFAULT_SALT_STRING = "<remove or keep as is for random>"
        const val DEFAULT_INTERNAL_HOSTS_SECRET_KEY = "<change required, otherwise reauth can be compromised!>"

        val DEFAULT = RASConfig(
            Database(
                host = "<host>",
                port = 5432,
                database = "<database>",
                username = "<username>",
                password = "<password>",
            ),
            Session(
                durationTime = 30.0,
                durationUnit = DurationUnit.MINUTES,
                sessionLength = 25,
                argon2salt = SESSION_DEFAULT_SALT_STRING,
            ),
            CORS(),
            TSL(),
            8080,
            DEFAULT_INTERNAL_HOSTS_SECRET_KEY,
            true,
        )

        fun load(path: Path = PATH): RASConfig =
            path.readAndDeserialize(serializer(), jsonForMachines)
    }
}


