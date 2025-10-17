# ReAuth Plugin API Reference

The ReAuth-Backend features a robust, extensible **plugin system** built around the `ReAuthPlugin` abstract class,
allowing developers to add custom functionality, routes, and modify server configuration via external JAR files.
All plugins must be packaged as JAR files and placed in the configured **plugins directory**.

Each plugin JAR must include a `plugin.json` file at its root with the following required fields:

- `name`: The name of your plugin
- `version`: Plugin version
- `author`: Plugin author
- `description`: (Optional) Plugin description
- `website`: (Optional) Plugin website, defaults to "https://example.com/"
- `mainClass`: The fully qualified name of your main plugin class, defaults to "com.example.reauth_plugin.MainKt"

-----

## Core Components

| Component            | Module                      | Description                                                                                                                                        |
|:---------------------|:----------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------|
| **`ReAuthPlugin`**   | `reprivatize.reauth.plugin` | The abstract base class that all plugins must extend. It provides methods for adding routes and configuring CORS settings.                         |
| **`PluginJSON`**     | `reprivatize.reauth.plugin` | A serializable data class for plugin metadata (name, version, author, main class) loaded from the plugin's configuration file.                     |
| **`reAuthServer()`** | `reprivatize.reauth`        | A global access function to the main `ReAuthServer` instance, providing access to `config`, `sessionService`, and mutable CORS configuration sets. |

-----

## The `ReAuthPlugin` Abstract Class

Plugins must implement two key lifecycle methods:

* **`abstract fun enable()`**: Called when the plugin is successfully loaded. This is where you register your custom
  routes and configure server settings.
* **`abstract fun disable()`**: Called when the plugin is being unloaded or the server is shutting down. Use this for
  cleanup and resource deallocation.

### Route Registration

The `routes()` function is the primary way to add custom Ktor REST endpoints:

| Function                              | Purpose                                                                                                        | Example Usage                                                        |
|:--------------------------------------|:---------------------------------------------------------------------------------------------------------------|:---------------------------------------------------------------------|
| `fun routes(block: Route.() -> Unit)` | Adds a block of Ktor routing logic to the server. The routes will be active at `/reauth/` level of the server. | `routes { get("/custom/ping") { call.respond(HttpStatusCode.OK) } }` |

**Example Route Implementation**

```kotlin
// Inside your ReAuthPlugin implementation's enable() method
override fun enable() {
    routes {
        // Define a simple GET endpoint at /reauth/api/status, reauth is ALWAYS the prefix.
        get("/api/status") {
            // Respond with HTTP 200 OK and a JSON object
            call.respond(
                HttpStatusCode.OK,
                mapOf("status" to "Plugin is Online", "version" to config.version)
            )
        }
    }
}
```

-----

## CORS and Server Configuration

Plugins can dynamically extend the server's Cross-Origin Resource Sharing (CORS) configuration using helper methods
within `ReAuthPlugin`. These changes will be applied to the Ktor `CORS` plugin instance.

| Function                                 | Purpose                                                                                   | Source Module  |
|:-----------------------------------------|:------------------------------------------------------------------------------------------|:---------------|
| `fun registerMethod(method: HttpMethod)` | Adds an HTTP method (e.g., `HttpMethod.Patch`) to the list of globally allowed methods.   | `io.ktor.http` |
| `fun registerHeader(header: String)`     | Adds a header to the list of globally allowed headers. User HttpHeaders.<...>             | N/A            |
| `fun registerHost(host: String)`         | Adds a specific host (origin) to the list of hosts allowed to make cross-origin requests. | N/A            |

**Example Configuration**

```kotlin
override fun enable() {
    // Allows clients to use the PATCH method
    registerMethod(HttpMethod.Patch)
    // Allows the custom X-Custom-Auth header
    registerHeader("X-Custom-Auth")
    registerHeader(HttpsHeaders.Authorization)
    registerHeader(RASHttpsHeaders.MIMEVersion) // The same as HttpHeaders.MIMEVersion
    // Allows requests from 'https://my-frontend.com'
    registerHost("https://my-frontend.com")
    // ... routes definition
}
```

-----

## Accessing Server Services

The global `reAuthServer()` function provides access to the server's core components:

* **`reAuthServer().config`**: Access to the loaded `RASConfig` (e.g., database details, session timeouts).
* **`reAuthServer().sessionService`**: Access to the `SessionService` for direct database interactions.

### Direct Session Management

Plugins can directly utilize the `SessionService` for secure session operations, which abstracts the Exposed/PostgreSQL
logic.

| Function                                                              | Purpose                                                                              | Security Note                                                                     |
|:----------------------------------------------------------------------|:-------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------|
| `suspend fun create(): String`                                        | Generates a new session and persists it. Returns the format: `internalId:sessionId`. | The `Argon2` hashing and session ID generation logic is internal to this service. |
| `suspend fun isValid(internalId: String, sessionId: String): Boolean` | Checks if a given session ID is valid and not expired.                               | This includes a secure `Argon2` verification.                                     |
| `suspend fun delete(internalId: Int)`                                 | Invalidates a session by its internal ID.                                            | Internal ID is the preferred way to manage a session.                             |