package org.coderva.reports.export

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.coderva.reports.database.ReportRepository
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.coderva.reports.models.StudentSessionCount

class ExcelReportGenerator(private val repository: ReportRepository){
    private val workbook = XSSFWorkbook()
    private val headerStyle: XSSFCellStyle by lazy {createHeaderStyle()}
    private val dateStyle: XSSFCellStyle by lazy {createDateStyle()}

    fun generateReport(outputPath: String, currentSchoolYear: Int = 25){
        println("Generating excel report")

        createOverviewSheet()
        createTeacherSummarySheet()
        createStudentSumarySheet()
        createDailyBreakdownSheet()

        FileOutputStream(outputPath).use { fileOut->
            workbook.write(fileOut)
        }
        workbook.close()
        println("Report generated. File name: $outputPath")

    }
    private fun createOverviewSheet(){
        val sheet = workbook.createSheet("Overview")
        var rowNum = 0

        
        sheet.createRow(rowNum++).createCell(0).apply{
            setCellValue("RR Tutoring Overview")
            cellStyle = createTitleStyle() 
        }
        rowNum++

        val (startDate, endDate) = repository.getDateRange()
        sheet.createRow(rowNum++).apply{
            createCell(0).setCellValue("Report Date Range: ")
            createCell(1).setCellValue("${startDate ?: "N/A"} to ${endDate ?: "N/A"}")
        }
        rowNum++
        //report date
        sheet.createRow(rowNum++).apply{
            createCell(0).setCellValue("Date Generated: ")
            createCell(1).setCellValue(LocalDate.now().format(DateTimeFormatter.ISO_DATE))
        }

        //total sessions
        val totalSessions = repository.getTotalSessionCount()
        sheet.createRow(rowNum++).apply{
            createCell(0).setCellValue("Total Sessions: ")
            createCell(1).setCellValue(totalSessions.toDouble())
        }
        rowNum++

        sheet.createRow(rowNum++).apply{
            createCell(0).apply{
                setCellValue("Sessions by Status: ")
                cellStyle = headerStyle
            }
        }
        val statusCounts = repository.getSessionsByStatus()
        statusCounts.forEach { (status, count)->
            sheet.createRow(rowNum++).apply {
                createCell(0).setCellValue(status.capitalize())
                createCell(1).setCellValue(count.toDouble())
                createCell(2).setCellValue("${(count.toDouble()/totalSessions*100).format(1)}%")
            }
        }
        rowNum++

        //sessions by lunch
        val lunchCounts = repository.getSessionsByLunchPeriod()
        lunchCounts.forEach{(lunch, count) ->
            sheet.createRow(rowNum++).apply{
                createCell(0).setCellValue(lunch)
                createCell(1).setCellValue(count.toDouble())
            }
        }

        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
        sheet.autoSizeColumn(2)

    }
    private fun createTeacherSummarySheet(){
        val sheet = workbook.createSheet("Teacher Summary")
        var rowNum = 0

        sheet.createRow(rowNum++).apply{
            createCell(0).apply{
                setCellValue("Teacher Name: ")
                cellStyle = headerStyle
            }
            createCell(1).apply{
                setCellValue("Sessions: ")
                cellStyle = headerStyle
            }
            createCell(2).apply{
                setCellValue("Percentile: ")
                cellStyle = headerStyle
            }
        }
        val sessionsByTeacher = repository.getSessionCountByTeacher()
        val percentiles = repository.getTeacherPercentiles()

        sessionsByTeacher.entries
            .sortedByDescending{it.value}
            .forEach{(teacher,count)->
                val row = sheet.createRow(rowNum++)
                row.createCell(0).setCellValue(teacher)
                row.createCell(1).setCellValue(count.toDouble())
                row.createCell(2).setCellValue("${percentiles[teacher] ?: 0}%")
            }
        
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
        sheet.autoSizeColumn(2)
        
    }
    private fun createStudentSumarySheet(){
        val sheet = workbook.createSheet("Student Summary")
        var rowNum = 0

        sheet.createRow(rowNum++).apply{
            createCell(0).apply{
                setCellValue("Student ID: ")
                cellStyle = headerStyle
            }
            createCell(1).apply{
                setCellValue("Student Name: ")
                cellStyle = headerStyle
            }
            createCell(2).apply{
                setCellValue("Sessions: ")
                cellStyle = headerStyle
            }
        }
        val studentSessions = repository.getStudentSessionCounts()
        studentSessions.forEach { student: StudentSessionCount ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(student.studentId.toDouble())
            row.createCell(1).setCellValue(student.studentName)
            row.createCell(2).setCellValue(student.sessionCount.toDouble())
        }
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
        sheet.autoSizeColumn(2)
    }
    private fun createDailyBreakdownSheet(){
        val sheet = workbook.createSheet("By Day Breakdown")
        var rowNum = 0
        sheet.createRow(rowNum++).apply{
            createCell(0).apply{
                setCellValue("Day of Week: ")
                cellStyle = headerStyle
            }
            createCell(1).apply{
                setCellValue("Sessions: ")
                cellStyle = headerStyle
            }
        }
        val days = listOf("MONDAY", "TUESDAY","WEDNESDAY","THURSDAY","FRIDAY")
        val sessionsByDay = repository.getSessionsByDayOfWeek()

        days.forEach{day ->
            val count = sessionsByDay[day] ?: 0
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(day)
            row.createCell(1).setCellValue(count.toDouble())
        }
        sheet.autoSizeColumn(0)
        sheet.autoSizeColumn(1)
    }

    private fun createTitleStyle(): XSSFCellStyle {
        return workbook.createCellStyle().apply{
            setFont(workbook.createFont().apply{
                bold = true 
                fontHeightInPoints = 16
            })
        }
    }
    private fun createHeaderStyle(): XSSFCellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply{
                bold = true
            })
            borderBottom = BorderStyle.THIN
        }
    }

    private fun createDateStyle(): XSSFCellStyle {
        return workbook.createCellStyle().apply{
            dataFormat = workbook.createDataFormat().getFormat("yyyy-mm-dd")
        }
    }
    private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
}