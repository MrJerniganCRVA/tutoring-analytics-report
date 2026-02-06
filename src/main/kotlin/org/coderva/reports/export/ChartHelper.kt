package org.coderva.reports.export

import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xddf.usermodel.chart.*
import org.apache.poi.xssf.usermodel.XSSFSheet


object ChartHelper {
    fun addBarChart(
        sheet: XSSFSheet,
        title: String,
        categoryRange: CellRangeAddress,
        valueRange: CellRangeAddress,
        chartPosition: ChartPosition
    ) {
        val drawing = sheet.createDrawingPatriarch()
        val anchor = drawing.createAnchor(
            0, 0, 0, 0,
            chartPosition.col1, chartPosition.row1,
            chartPosition.col2, chartPosition.row2
        )
        val chart = drawing.createChart(anchor)
        chart.setTitleText(title)
        chart.setTitleOverlay(false)

        val categories = org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromStringCellRange(sheet, categoryRange)
        val values = org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory.fromNumericCellRange(sheet, valueRange)

        val xAxis = chart.createCategoryAxis(AxisPosition.BOTTOM)
        val yAxis = chart.createValueAxis(AxisPosition.LEFT)
        yAxis.crosses = AxisCrosses.AUTO_ZERO

        val barChart = chart.createData(ChartTypes.BAR, xAxis, yAxis) as XDDFBarChartData
        barChart.barDirection = BarDirection.COL 

        val series = barChart.addSeries(categories, values)
        series.setTitle(title, null)

        chart.plot(barChart)
    }
    
}

data class ChartPosition(
    val col1: Int,
    val row1: Int,
    val col2: Int,
    val row2: Int
)