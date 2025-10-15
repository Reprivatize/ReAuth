/*
 *     ReAuth-Backend: ReAuthServer.kt
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

import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.hsts.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.routing.*
import mtctx.pluggable.Pluggable
import mtctx.utilities.Outcome
import mtctx.utilities.fileSystem
import mtctx.utilities.jsonForMachines
import okio.Path
import okio.Path.Companion.toOkioPath
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reprivatize.web.database.SessionService
import reprivatize.web.plugin.PluginJSON
import reprivatize.web.plugin.ReAuthPlugin
import reprivatize.web.route.session
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.toPath
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

val RUNNING_DIR: Path = ReAuthServer::class.java.protectionDomain.codeSource.location.toURI().toPath().toOkioPath()
const val RAS_ASCII =
    """ ________  _______        ________  ___  ___  _________  ___  ___
|\   __  \|\  ___ \   ___|\   __  \|\  \|\  \|\___   ___\\  \|\  \
\ \  \|\  \ \   __/| |\__\ \  \|\  \ \  \\\  \|___ \  \_\ \  \\\  \
 \ \   _  _\ \  \_|/_\|__|\ \   __  \ \  \\\  \   \ \  \ \ \   __  \
  \ \  \\  \\ \  \_|\ \  __\ \  \ \  \ \  \\\  \   \ \  \ \ \  \ \  \
   \ \__\\ _\\ \_______\|\__\ \__\ \__\ \_______\   \ \__\ \ \__\ \__\
    \|__|\|__|\|_______|\|__|\|__|\|__|\|_______|    \|__|  \|__|\|__|"""

class ReAuthServer {
    val config: RASConfig
    val reAuthLogger: Logger = LoggerFactory.getLogger("ReAuth")

    val db: Database
    val sessionService: SessionService

    val plugins: MutableList<Plugin> = mutableListOf()
    val pluginRoutes = mutableListOf<Route.() -> Unit>()
    val pluginDir =
        RUNNING_DIR.resolve("plugins").toNioPath()
    val pluggable = Pluggable(
        pluginDir,
        PluginJSON.serializer(),
        ReAuthPlugin::class.java,
        ReAuthServer::class.java
    )

    // Configurable by plugins
    val allowedMethods = mutableSetOf<HttpMethod>()
    val allowedHeaders = mutableSetOf<String>()
    val allowedHosts = mutableSetOf<String>()


    init {
        RAS_ASCII.split("\n").forEach { reAuthLogger.info(it) }
        reAuthLogger.info("Starting ReAuth Backend...")
        reAuthLogger.info("Version: 1.0.0")
        reAuthServer = this

        reAuthLogger.info("Loading configuration...")
        if (!fileSystem.exists(RASConfig.PATH)) {
            RASConfig.DEFAULT.save()
            reAuthLogger.info("Created default configuration, please edit it!")
            reAuthLogger.info(fileSystem.canonicalize(RASConfig.PATH).toString())
            exitProcess(0)
        }

        config = RASConfig.load()
        if (config == RASConfig.DEFAULT) {
            reAuthLogger.info("Default configuration detected, please edit it!")
            reAuthLogger.info(fileSystem.canonicalize(RASConfig.PATH).toString())
            exitProcess(1)
        }

        if (config.internalHostsSecretKey == RASConfig.DEFAULT_INTERNAL_HOSTS_SECRET_KEY) {
            reAuthLogger.info("Default internal hosts secret key detected, you NEED to change it, otherwise reauth can be compromised!")
            reAuthLogger.info(fileSystem.canonicalize(RASConfig.PATH).toString())
            exitProcess(1)
        }

        reAuthLogger.info("Loaded configuration!")

        reAuthLogger.info("Populating allowed http methods, http headers, and hosts...")
        allowedMethods += config.cors.allowedMethods.map { it.ktor }
        allowedHeaders += config.cors.allowedHeaders.map { it.ktor }
        allowedHosts += config.cors.allowedHosts
        reAuthLogger.info("Populated allowed http methods, http headers, and hosts!")

        reAuthLogger.info("Connecting to database...")
        db = Database.connect(
            url = "jdbc:postgresql://${config.database.host}:${config.database.port}/${config.database.database}",
            driver = "org.postgresql.Driver",
            user = config.database.username,
            password = config.database.password,
        )
        sessionService = SessionService(db)
        reAuthLogger.info("Connected to database!")

        val pluginsToLoad = pluginDir.exists() && !pluginDir.listDirectoryEntries().isEmpty()
        if (!pluginsToLoad) reAuthLogger.info("No plugins to load!")
        else {
            reAuthLogger.info("Loading plugins...")
            val loadOutcomes = pluggable.loadAll()
            loadOutcomes.values.filterIsInstance<Outcome.Success<Plugin>>().map { it.value }.forEach {
                plugins.add(it)
                it.plugin.enable()
                reAuthLogger.info("Loaded and enabled plugin ${it.config.name} v${it.config.version}")
            }

            loadOutcomes.forEach { (fileName, outcome) ->
                if (outcome.failed) {
                    outcome as Outcome.Failure
                    reAuthLogger.error("Failed to load $fileName: ${outcome.message}", outcome.throwable)
                }
            }
            reAuthLogger.info("Loaded plugins!")
        }

        reAuthLogger.info("Starting web server...")
        embeddedServer(CIO, port = config.port, host = "0.0.0.0") {
            configureSerialization()
            configureAdministration()
            configureHTTP()
            configureRouting()
        }.start(wait = true).also { reAuthLogger.info("Started web server!") }

        reAuthLogger.info("Stopping web server...")

        if (pluginsToLoad) {
            reAuthLogger.info("Disabling and unloading ${plugins.size} plugins...")
            plugins.forEach {
                it.plugin.disable()
                when (val outcome = pluggable.unload(it)) {
                    is Outcome.Success -> reAuthLogger.info("Unloaded plugin ${it.config.name} v${it.config.version}")
                    is Outcome.Failure -> reAuthLogger.error(
                        "Failed to unload plugin ${it.config.name} v${it.config.version}",
                        outcome.message,
                        outcome.throwable
                    )
                }
            }
        }

        reAuthLogger.info("Stopped web server!")
    }

    private fun Application.configureAdministration() {
        install(RateLimiting) {
            rateLimiter {
                type = TokenBucket::class
                capacity = 100
                rate = 10.seconds
            }
        }
    }

    private fun Application.configureHTTP() {
        install(Compression)
        install(CORS) {
            allowedMethods.forEach { allowMethod(it) }
            allowedHeaders.forEach { allowHeader(it) }
            allowedHosts.forEach { allowHost(it) }
        }
        install(HSTS) {
            includeSubDomains = true
        }
        install(HttpsRedirect) {
            sslPort = 443
            permanentRedirect = true
        }
    }

    private fun Application.configureSerialization() {
        install(ContentNegotiation) {
            json(jsonForMachines)
        }
    }

    private fun Application.configureRouting() {
        val prefix = "/reauth"
        routing {
            route("/$prefix") {
                session()

                pluginRoutes.forEach { it() }
            }
        }
    }
}