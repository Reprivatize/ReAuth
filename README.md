# ReAuth – Secure & Extensible Session Backend

## What is ReAuth-Backend?

**ReAuth-Backend** is a secure, Kotlin-based server application built on **Ktor** and **Exposed** (for database access)
designed to handle robust and extensible **session management**.

Its primary function is to provide a dedicated, secure service for:

* **Creating** and **validating** highly secure session IDs using **Argon2** hashing.
* Integrating with a **PostgreSQL** database to persist session data.
* Offering an **extensible plugin system** to customize and add new features via external JARs.

It is ideal for applications that need a centralized, high-performance, and pluggable authentication and session
verification endpoint.

-----

## Modules & Structure

| Component                          | Description                                                                                                                                            |
|:-----------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`reprivatize.web.crypto`**       | Logic for secure operations, including the `generateSessionId` function using `SECURE_RANDOM`.                                                         |
| **`reprivatize.web.database`**     | Handles persistent storage, defining the `Session` data class and the `SessionService` for all database interactions (create, read, delete, validate). |
| **`reprivatize.web.plugin`**       | Core interfaces for the extensibility model: `ReAuthPlugin` abstract class and `PluginJSON` for metadata configuration.                                |
| **`reprivatize.web.route`**        | Defines the Ktor REST endpoints, notably for `/reauth/session/create`, `/reauth/session/valid`, and `/reauth/session/invalidate`.                      |
| **`reprivatize.web.ReAuthServer`** | The main application entry point, handling configuration loading, database connection, plugin loading/management, and Ktor server setup.               |

-----

## Features

* 🔒 **Secure Sessions:** Uses **Argon2** for hashing session IDs with a per-session salt, ensuring robust security.
* 🔌 **Pluggable Architecture:** Allows loading external `.jar` plugins based on the `ReAuthPlugin` interface to extend
  routing and functionality.
* ⏱️ **Session Timeouts:** Configurable session durations with automatic validity checking.
* 💨 **Ktor & CIO:** Built on the Ktor framework and the CIO engine for high-performance and asynchronous I/O.
* 💾 **PostgreSQL Ready:** Uses the **Exposed** SQL framework for seamless integration with PostgreSQL.
* ⚙️ **Secure Internal Access:** Implements an `isInternalSecretBased()` check using a configurable secret key to
  restrict sensitive endpoints (like `/session/create`) to trusted hosts.

-----

## Configuration

Configuration is handled via the `config.json` file, managed by the `RASConfig` data class.

### Key Configuration Fields

| Field                        | Description                                                                                         | Default Value (approx.)             |
|:-----------------------------|:----------------------------------------------------------------------------------------------------|:------------------------------------|
| **`database`**               | PostgreSQL connection details (host, port, database, username, password).                           | `<must be configured>`              |
| **`session`**                | Controls session length and duration (`durationTime`, `durationUnit`).                              | 30.0 MINUTES                        |
| **`internalHostsSecretKey`** | **CRITICAL:** A secret key used to secure internal API endpoints. **Must be changed from default.** | `DEFAULT_INTERNAL_HOSTS_SECRET_KEY` |
| **`port`**                   | The network port the Ktor server binds to.                                                          | 8080                                |

-----

## API Endpoints

The primary API endpoints are available under the `/reauth` prefix.

| Method   | Endpoint                     | Description                                                                                                                 | Security                     |
|:---------|:-----------------------------|:----------------------------------------------------------------------------------------------------------------------------|:-----------------------------|
| `POST`   | `/reauth/session/create`     | Generates a new, unique, and secure session ID and persists it in the database. Returns the format: `internalId:sessionId`. | **Internal Secret Required** |
| `POST`   | `/reauth/session/valid`      | Validates if a provided session ID is correct and has not yet expired.                                                      | Public                       |
| `DELETE` | `/reauth/session/invalidate` | Invalidates (deletes) an active session from the database using its internal ID.                                            | Public                       |

-----

## Documentation

Full API usage and reference are available at:

* 📖 **Plugin API Usage & Examples:** See [PLUGIN\_API\_DOC.md](PLUGIN_API_DOC.md)
* 👉 **API Reference (Dokka):** [http://reauth.apidoc.reprivatize.me/](http://reauth.apidoc.reprivatize.me/)

-----

## License

ReAuth-Backend is free software under the **GNU GPL v3**.
You can use it, modify it, and distribute it — as long as it remains free.