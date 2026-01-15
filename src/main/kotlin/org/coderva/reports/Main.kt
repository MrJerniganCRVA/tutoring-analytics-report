package org.coderva.reports

import org.coderva.reports.database.DatabaseConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    println("Tutoring Analytics Report Generator")
    println()
    //TODO: add report info

    try{
        DatabaseConfig.connect()
        transaction {
            val count = exec("SELECT COUNT(*) FROM \"TutoringRequests\"") { rs ->
                if(rs.next()) rs.getLong(1) else 0
            }
            println("Connection success")
            println("Total sessions found: $count")

            exec("SELECT * FROM \"TutoringRequests\" LIMIT 1") { rs ->
                if(rs.next()){
                    println("Sample Data")
                    val metadata = rs.metaData
                    for (i in 1..metadata.columnCount){
                        println("${metadata.getColumnName(i)}: ${rs.getString(i)}")
                    }
                }

            }
        }
        println("Success!")
    } catch (e: Exception){
        println("Error!!")
        println(e.message)
        e.printStackTrace()
    }
}