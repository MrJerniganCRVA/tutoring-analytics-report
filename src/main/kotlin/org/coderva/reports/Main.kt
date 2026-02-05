package org.coderva.reports

import org.coderva.reports.export.ExcelReportGenerator

import org.coderva.reports.database.ReportRepository
import org.coderva.reports.database.DatabaseConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun main() {
    println("Tutoring Analytics Report Generator")
    println()
    //TODO: add report info
    
    DatabaseConfig.connect()
    
    val repo = ReportRepository()
    val excelGenerator = ExcelReportGenerator(repo)

    val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val outputPath = "tutoring_report_$timestamp.xlsx"

    excelGenerator.generateReport(outputPath, currentSchoolYear = 25)

    println("Report Complete!")
    println("Find file at: $outputPath")
    

}