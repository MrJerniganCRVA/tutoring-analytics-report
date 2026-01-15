package org.coderva.reports.database

import org.jetbrains.exposed.sql.Database
import java.util.Properties

object DatabaseConfig{
    fun connect() {
        val dbUrl = System.getenv("DATABASE_URL")
            ?: loadFromConfig("db.url")
        val dbUser = System.getenv("DB_USER")
            ?:  loadFromConfig("db.user")
        val dbPassword = System.getenv("DB_PASSWORD")
            ?: loadFromConfig("db.password")
        val props = Properties()
        props.load(DatabaseConfig::class.java.classLoader.getResourceAsStream("config.properties"))
        Database.connect(
            url = "$dbUrl?sslmode=require&ssl=true",
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword
        )
        println("Database connected! Good job!")
    }
    private fun loadFromConfig(key: String): String{
        val props = Properties()
        props.load(DatabaseConfig::class.java.classLoader.getResourceAsStream("config.properties"))
        return props.getProperty(key)
    }
}