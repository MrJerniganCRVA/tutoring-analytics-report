package org.coderva.reports.database

import org.jetbrains.exposed.sql.Database
import java.net.URI
import java.net.URLDecoder
import java.util.Properties

object DatabaseConfig{
    // Environment first (Railway), then src/main/resources/config.properties for
    // local runs. The properties file is optional - it is gitignored, so it does
    // not exist inside the deployed container.
    fun connect() {
        val rawUrl = setting("DATABASE_URL", "db.url")
            ?: error("DATABASE_URL (or db.url in config.properties) is not set")
        val (jdbcUrl, urlUser, urlPassword) = toJdbc(rawUrl)

        val dbUser = setting("DB_USER", "db.user") ?: urlUser
        val dbPassword = setting("DB_PASSWORD", "db.password") ?: urlPassword

        // Railway's private network (postgres.railway.internal) is not TLS, while
        // the public proxy URL needs it. DB_SSL=false turns it off; anything else
        // keeps the original always-require behavior.
        val useSsl = System.getenv("DB_SSL")?.lowercase() != "false"
        val separator = if (jdbcUrl.contains("?")) "&" else "?"
        val url = if (useSsl) "$jdbcUrl${separator}sslmode=require&ssl=true" else jdbcUrl

        Database.connect(
            url = url,
            driver = "org.postgresql.Driver",
            user = dbUser ?: "",
            password = dbPassword ?: ""
        )
        println("Database connected! Good job!")
    }

    // Railway hands out postgres://user:pass@host:port/db; JDBC wants
    // jdbc:postgresql://host:port/db with the credentials passed separately.
    private fun toJdbc(raw: String): Triple<String, String?, String?> {
        if (raw.startsWith("jdbc:")) return Triple(raw, null, null)
        val uri = URI(raw)
        val userInfo = uri.rawUserInfo?.split(":", limit = 2)
        val decode = { s: String -> URLDecoder.decode(s, Charsets.UTF_8) }
        val port = if (uri.port == -1) 5432 else uri.port
        val query = uri.rawQuery?.let { "?$it" } ?: ""
        return Triple(
            "jdbc:postgresql://${uri.host}:$port${uri.rawPath}$query",
            userInfo?.getOrNull(0)?.let(decode),
            userInfo?.getOrNull(1)?.let(decode)
        )
    }

    private fun setting(envKey: String, propKey: String): String? =
        System.getenv(envKey)?.takeIf { it.isNotBlank() } ?: fileProps.getProperty(propKey)

    private val fileProps: Properties by lazy {
        Properties().apply {
            DatabaseConfig::class.java.classLoader.getResourceAsStream("config.properties")?.use { load(it) }
        }
    }
}
