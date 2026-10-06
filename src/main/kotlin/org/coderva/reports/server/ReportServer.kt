package org.coderva.reports.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.coderva.reports.currentSchoolYear
import org.coderva.reports.database.ReportRepository
import org.coderva.reports.export.ExcelReportGenerator
import java.net.InetSocketAddress
import java.nio.file.Files
import java.security.MessageDigest
import java.util.concurrent.Executors

// A deliberately tiny HTTP front end (JDK built-in server, no extra
// dependencies) so the report can run on demand as a Railway service.
//
// It is meant to be reached only over Railway's private network by the RR
// Tutoring server, which checks the caller is an admin before proxying. The
// shared REPORT_TOKEN is the second lock, in case the service is ever given a
// public domain by mistake.
object ReportServer {
    private const val XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

    // Generating holds the whole workbook in memory; one at a time keeps a
    // double-click from doubling the heap on a small container.
    private val generateLock = Any()

    fun start() {
        val token = System.getenv("REPORT_TOKEN")?.takeIf { it.isNotBlank() }
            ?: error("REPORT_TOKEN must be set to run the report server")
        val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

        // Wildcard address: the JVM binds dual-stack where IPv6 exists, which
        // Railway's private network needs, and plain IPv4 where it doesn't.
        val server = HttpServer.create(InetSocketAddress(port), 0)
        server.executor = Executors.newFixedThreadPool(4)

        server.createContext("/health") { exchange ->
            exchange.use { respondText(it, 200, "ok") }
        }

        server.createContext("/report") { exchange ->
            exchange.use {
                when {
                    it.requestMethod != "GET" -> respondText(it, 405, "Method not allowed")
                    !tokenMatches(it.requestHeaders.getFirst("X-Report-Token"), token) ->
                        respondText(it, 401, "Unauthorized")
                    else -> sendReport(it)
                }
            }
        }

        server.start()
        println("Report server listening on port $port")
    }

    private fun sendReport(exchange: HttpExchange) {
        val file = Files.createTempFile("tutoring_report_", ".xlsx")
        try {
            synchronized(generateLock) {
                // The generator owns a single workbook, so it is built per request.
                ExcelReportGenerator(ReportRepository()).generateReport(file.toString(), currentSchoolYear())
            }
            exchange.responseHeaders.add("Content-Type", XLSX)
            exchange.sendResponseHeaders(200, Files.size(file))
            exchange.responseBody.use { out -> Files.copy(file, out) }
        } catch (e: Exception) {
            System.err.println("Report generation failed: ${e.message}")
            e.printStackTrace()
            respondText(exchange, 500, "Report generation failed")
        } finally {
            Files.deleteIfExists(file)
        }
    }

    private fun tokenMatches(given: String?, expected: String): Boolean =
        given != null && MessageDigest.isEqual(given.toByteArray(), expected.toByteArray())

    private fun respondText(exchange: HttpExchange, status: Int, body: String) {
        val bytes = body.toByteArray()
        exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { it.write(bytes) }
    }
}
