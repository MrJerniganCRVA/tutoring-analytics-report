package org.coderva.reports

import org.coderva.reports.export.ExcelReportGenerator
import org.coderva.reports.database.ReportRepository
import org.coderva.reports.database.DatabaseConfig
import org.coderva.reports.server.ReportServer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// `serve` (the Docker default) runs the HTTP endpoint the RR Tutoring app's
// "Download full report" button calls. No argument keeps the original
// behavior: generate the workbook once into the current directory.
fun main(args: Array<String>) {
    println("Tutoring Analytics Report Generator")
    println()

    DatabaseConfig.connect()

    if (args.firstOrNull() == "serve") {
        ReportServer.start()
        return
    }

    val timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val outputPath = "tutoring_report_$timestamp.xlsx"

    ExcelReportGenerator(ReportRepository()).generateReport(outputPath, currentSchoolYear())

    println("Report Complete!")
    println("Find file at: $outputPath")
}

// Two-digit year the current school year started in (Aug-Jul), matching the
// two-digit entry-year prefix on student ids: Oct 2026 -> 26, Mar 2027 -> 26.
fun currentSchoolYear(today: LocalDate = LocalDate.now()): Int {
    val startYear = if (today.monthValue >= 8) today.year else today.year - 1
    return startYear % 100
}
