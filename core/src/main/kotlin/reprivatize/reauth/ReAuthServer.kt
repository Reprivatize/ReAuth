/*
 * ReAuth-Backend (ReAuth-Backend.core.main): ReAuthServer.kt
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

@file:OptIn(ExperimentalTime::class)

package reprivatize.reauth

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
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mtctx.pluggable.Pluggable
import mtctx.utilities.Outcome
import mtctx.utilities.fileSystem
import mtctx.utilities.jsonForMachines
import okio.Path
import okio.Path.Companion.toPath
import org.jetbrains.exposed.sql.Database
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reprivatize.reauth.plugin.PluginConfig
import reprivatize.reauth.service.RASSessionService
import reprivatize.reauth.session.Session
import reprivatize.reauth.session.SessionCheckMiddleware
import reprivatize.reauth.session.SessionService
import reprivatize.reauth.session.session
import java.sql.DriverManager
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.system.exitProcess
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

internal val RUNNING_DIR: Path = fileSystem.canonicalize("./".toPath())
internal val RAS_ASCII = """
________  _______        ________  ___  ___  _________  ___  ___
|\   __  \|\  ___ \   ___|\   __  \|\  \|\  \|\___   ___\\  \|\  \
\ \  \|\  \ \   __/| |\__\ \  \|\  \ \  \\\  \|___ \  \_\ \  \\\  \
 \ \   _  _\ \  \_|/_\|__|\ \   __  \ \  \\\  \   \ \  \ \ \   __  \
  \ \  \\  \\ \  \_|\ \  __\ \  \ \  \ \  \\\  \   \ \  \ \ \  \ \  \
   \ \__\\ _\\ \_______\|\__\ \__\ \__\ \_______\   \ \__\ \ \__\ \__\
    \|__|\|__|\|_______|\|__|\|__|\|__|\|_______|    \|__|  \|__|\|__|
""".trimEnd().trimStart()

private val internalWhenSessionCached = mutableMapOf<UUID, Instant>()
private val internalCachedSessions = mutableMapOf<UUID, Session>()
val cachedSessions: Map<UUID, Session> get() = internalCachedSessions
val sessionMutex = Mutex()

suspend fun cacheSession(session: Session) = sessionMutex.withLock {
    internalWhenSessionCached[session.uuid] = Clock.System.now()
    internalCachedSessions[session.uuid] = session
}

suspend fun uncacheSession(uuid: UUID) = sessionMutex.withLock {
    internalWhenSessionCached.remove(uuid)
    internalCachedSessions.remove(uuid)
}

class ReAuthServer : RASHost() {
    val config: RASConfig
    val logger: Logger = LoggerFactory.getLogger("ReAuth")

    val db: Database
    val sessionService: RASSessionService

    val plugins: MutableList<Plugin> = mutableListOf()
    val pluginRoutes = mutableListOf<Route.() -> Unit>()
    val pluginDir =
        RUNNING_DIR.resolve("plugins").toNioPath()

    val pluggable = Pluggable(
        pluginDir,
        PluginConfig.serializer(),
        ReAuthPlugin::class.java,
        RASHost::class.java
    )

    // Configurable by plugins
    val allowedMethods = mutableSetOf<HttpMethod>()
    val allowedHeaders = mutableSetOf<String>()
    val allowedHosts = mutableSetOf<String>()
    val blacklistedRoutesForMiddleware = mutableSetOf("/reauth/session/validate")

    init {
        RAS_ASCII.split("\n").forEach { logger.info(it) }
        logger.info("Starting ReAuth Backend...")
        logger.info("Version: 1.0.0")
        reAuthServer = this

        logger.info("Loading configuration...")
        if (!fileSystem.exists(RASConfig.PATH)) {
            RASConfig.DEFAULT.save()
            logger.info("Created default configuration, please edit it!")
            logger.info(fileSystem.canonicalize(RASConfig.PATH).toString())
            exitProcess(0)
        }

        config = RASConfig.load()
        if (config == RASConfig.DEFAULT) {
            logger.info("Default configuration detected, please edit it!")
            logger.info(fileSystem.canonicalize(RASConfig.PATH).toString())
            exitProcess(1)
        }

        if (config.internalHostsSecretKey == RASConfig.DEFAULT_INTERNAL_HOSTS_SECRET_KEY) {
            logger.info("Default internal hosts secret key detected, you NEED to change it, otherwise reauth can be compromised!")
            logger.info(fileSystem.canonicalize(RASConfig.PATH).toString())
            exitProcess(1)
        }

        logger.info("Loaded configuration!")

        logger.info("Populating allowed http methods, http headers, and hosts...")
        allowedMethods += config.cors.allowedMethods.map { it.ktor }
        allowedHeaders += config.cors.allowedHeaders.map { it.ktor }
        allowedHosts += config.cors.allowedHosts
        logger.info("Populated allowed http methods, http headers, and hosts!")

        logger.info("Connecting to database...")

        try {
            Class.forName("org.postgresql.Driver")
            DriverManager.getConnection(
                "jdbc:postgresql://${config.database.host}:${config.database.port}/${config.database.database}",
                config.database.username,
                config.database.password
            )
        } catch (e: Exception) {
            logger.error("Failed to connect to database! Check if your configuration is correct and if the database is online.")
            exitProcess(1)
        }

        db = Database.connect(
            url = "jdbc:postgresql://${config.database.host}:${config.database.port}/${config.database.database}",
            driver = "org.postgresql.Driver",
            user = config.database.username,
            password = config.database.password,
        )
        sessionService = SessionService(db)

        logger.info("Connected to database!")

        val pluginsToLoad = pluginDir.exists() && !pluginDir.listDirectoryEntries().isEmpty()
        if (!pluginsToLoad) logger.info("No plugins to load!")
        else {
            logger.info("Loading plugins...")
            val loadOutcomes = pluggable.loadAll()
            loadOutcomes.values.filterIsInstance<Outcome.Success<Plugin>>().map { it.value }.forEach {
                plugins.add(it)
                it.plugin.initialize(this, LoggerFactory.getLogger(it.config.name), sessionService)
                it.plugin.enable()
                logger.info("Loaded and enabled plugin ${it.config.name} v${it.config.version}")
            }

            loadOutcomes.forEach { (fileName, outcome) ->
                if (outcome.failed) {
                    outcome as Outcome.Failure
                    logger.error("Failed to load $fileName: ${outcome.message}", outcome.throwable)
                }
            }
            logger.info("Loaded plugins!")
        }

        blacklistedRoutesForMiddleware.remove("/reauth/session/valid")
        blacklistedRoutesForMiddleware.remove("/reauth/session/invalidate")

        logger.info("Starting web server...")
        embeddedServer(CIO, port = config.port, host = "0.0.0.0") {
            configureSerialization()
            configureAdministration()
            configureHTTP()
            install(SessionCheckMiddleware)
            configureRouting()

            val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            var cacheCleanupJob: Job? = null

            monitor.subscribe(ApplicationStarted) {
                cacheCleanupJob = coroutineScope.launch {
                    while (isActive) {
                        sessionMutex.withLock {
                            internalWhenSessionCached.entries.toList()
                                .forEach { (uuid, instant) ->
                                    if (
                                        instant + config.session.clearSessionCacheDuration() > Clock.System.now()
                                        || internalCachedSessions[uuid] == null
                                    ) return@forEach
                                    internalWhenSessionCached.remove(uuid)
                                    internalCachedSessions.remove(uuid)
                                }
                        }
                        delay(config.session.clearSessionCacheDuration())
                    }
                }
            }

            monitor.subscribe(ApplicationStopped) {
                cacheCleanupJob?.cancel()
            }
        }.start(wait = true)

        logger.info("Stopping web server...")

        if (pluginsToLoad) {
            logger.info("Disabling and unloading ${plugins.size} plugins...")
            plugins.forEach {
                it.plugin.disable()
                when (val outcome = pluggable.unload(it)) {
                    is Outcome.Success -> logger.info("Unloaded plugin ${it.config.name} v${it.config.version}")
                    is Outcome.Failure -> logger.error(
                        "Failed to unload plugin ${it.config.name} v${it.config.version}",
                        outcome.message,
                        outcome.throwable
                    )
                }
            }
        }

        logger.info("Stopped web server!")
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
        routing {
            route("/reauth") {
                session()

                pluginRoutes.forEach { it() }
            }
        }
    }

    override fun addPluginRoute(route: Route.() -> Unit) {
        this.pluginRoutes.add(route)
    }

    override fun blacklistRoutesForMiddleware(routes: List<String>) {
        this.blacklistedRoutesForMiddleware += routes.map {
            if (it.startsWith("/reauth/")) it
            else if (it.startsWith("/")) "/reauth$it"
            else "/reauth/$it"
        }
    }

    override fun registerMethods(methods: List<HttpMethod>) {
        this.allowedMethods += methods
    }

    override fun registerHeaders(headers: List<String>) {
        this.allowedHeaders += headers
    }

    override fun registerHosts(hosts: List<String>) {
        this.allowedHosts += hosts
    }
}