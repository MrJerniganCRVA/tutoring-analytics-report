package org.coderva.reports

import org.coderva.reports.database.DatabaseConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    println("Tutoring Analytics Report Generator")
    println()
    //TODO: add report info
    
    DatabaseConfig.connect()
    
    val repo = ReportRepository()
    //Testing a few queries
    println("Sessions by Department:")
    repo.getSessionCountByDepartment().forEach { (dept, count) ->
        println("  $dept: $count")
    }
    
    println("\nTop 10 Most Requested Subjects:")
    repo.getMostRequestedSubjects(10).forEach { (subject, count) ->
        println("  $subject: $count")
    }

}