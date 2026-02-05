package org.coderva.reports.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.coderva.reports.models.*
import java.time.LocalDate


class ReportRepository {
    fun getSessionCountByDepartment(): Map<String, Long> = transaction {
        (Teachers innerJoin TutoringRequests)
            .slice(Teachers.subject, TutoringRequests.id.count())
            .selectAll()
            .groupBy(Teachers.subject)
            .associate{
                it[Teachers.subject] to it[TutoringRequests.id.count()]
            }
    }

    fun getSessionTrendsByDepartment(startDate: LocalDate, endDate:LocalDate): List<DepartmentTrend> = transaction{
        (Teachers innerJoin TutoringRequests)
            .slice(Teachers.subject, TutoringRequests.date, TutoringRequests.id.count())
            .select{TutoringRequests.date.between(startDate, endDate)}
            .groupBy(Teachers.subject, TutoringRequests.date)
            .orderBy(TutoringRequests.date to SortOrder.ASC)
            .map { row -> 
                DepartmentTrend(
                    department = row[Teachers.subject],
                    date = row[TutoringRequests.date],
                    sessionCount = row[TutoringRequests.id.count()]
                )
            }
    }
    fun getSessionCountByTeacher(): Map<String, Long> = transaction {
        (Teachers innerJoin TutoringRequests)
            .slice(Teachers.firstName, Teachers.lastName, TutoringRequests.id.count())
            .selectAll()
            .groupBy(Teachers.id)
            .associate { row ->
                "${row[Teachers.firstName]} ${row[Teachers.lastName]}" to row[TutoringRequests.id.count()] 
            }
    }
    
    fun getTopStudentsByTeacher(teacherId: Int, limit: Int = 10): List<StudentSessionCount> = transaction {
        (Students innerJoin TutoringRequests)
            .slice(Students.firstName, Students.lastName, Students.id, TutoringRequests.id.count())
            .select { TutoringRequests.teacherId eq teacherId }
            .groupBy(Students.id)
            .orderBy(TutoringRequests.id.count() to SortOrder.DESC)
            .limit(limit)
            .map {  row ->
                StudentSessionCount(
                    studentId = row[Students.id],
                    studentName = "${row[Students.firstName]} ${row[Students.lastName]}",
                    sessionCount = row[TutoringRequests.id.count()]
                )
            }
    }
    
    fun getTeacherPercentiles(): Map<String, Int> = transaction {
        val sessionCounts = getSessionCountByTeacher()
        val sortedCounts = sessionCounts.values.sorted()
        
        sessionCounts.mapValues { (_, count) ->
            val rank = sortedCounts.indexOf(count)
            ((rank.toDouble() / sortedCounts.size) * 100).toInt()
        }
    }
    
    fun getStudentSessionCounts(): List<StudentSessionCount> = transaction {
        (Students innerJoin TutoringRequests)
            .slice(Students.id, Students.firstName, Students.lastName, TutoringRequests.id.count())
            .selectAll()
            .groupBy(Students.id)
            .orderBy(TutoringRequests.id.count() to SortOrder.DESC)
            .map { row ->
                StudentSessionCount(
                    studentId = row[Students.id],
                    studentName = "${row[Students.firstName]} ${row[Students.lastName]}",
                    sessionCount = row[TutoringRequests.id.count()]
                )
            }
    }
    
    fun getStudentsWithoutSessions(): List<Student> = transaction {
        Students
            .leftJoin(TutoringRequests)
            .slice(Students.id, Students.firstName, Students.lastName)
            .select { TutoringRequests.id.isNull() }
            .map { row ->
                Student(
                    id = row[Students.id],
                    firstName = row[Students.firstName],
                    lastName = row[Students.lastName]
                )
            }
    }
    
    
    fun getSessionsByDate(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Long> = transaction {
        TutoringRequests
            .slice(TutoringRequests.date, TutoringRequests.id.count())
            .select { TutoringRequests.date.between(startDate, endDate) }
            .groupBy(TutoringRequests.date)
            .associate { row ->
                row[TutoringRequests.date] to row[TutoringRequests.id.count()] 
            }
    }
    
    
    fun getSessionsByGradeLevel(currentSchoolYear: Int): Map<String, Long> = transaction {
        (Students innerJoin TutoringRequests)
            .slice(Students.id, TutoringRequests.id.count())
            .selectAll()
            .groupBy(Students.id)
            .map { row ->
                val studentId = row[Students.id]
                val gradeLevel = Student(studentId, "", "").getGradeLevel(currentSchoolYear)
                Pair(gradeLevel, row[TutoringRequests.id.count()])
            }
            .groupBy({ pair: Pair<String, Long> -> pair.first }, { pair: Pair<String, Long> -> pair.second })
            .mapValues { entry: Map.Entry<String, List<Long>> -> entry.value.sum() }
    }

    fun getSessionsByDayOfWeek(): Map<String, Long> = transaction{
        TutoringRequests
            .slice(TutoringRequests.date, TutoringRequests.id.count())
            .selectAll()
            .groupBy(TutoringRequests.date)
            .map {row ->
                val date = row[TutoringRequests.date]
                val dayOfWeek = date.dayOfWeek.toString()
                dayOfWeek to row[TutoringRequests.id.count()]
            }
            .groupBy({it.first}, {it.second})
            .mapValues{it.value.sum()}
    }
    fun getSessionsByLunchPeriod(): Map<String, Long> = transaction {
        val sessions = TutoringRequests.selectAll().toList()

        mapOf(
            "Lunch A" to sessions.count{it[TutoringRequests.lunchA]},
            "Lunch B" to sessions.count{it[TutoringRequests.lunchB]},
            "Lunch C" to sessions.count{it[TutoringRequests.lunchC]},
            "Lunch D" to sessions.count{it[TutoringRequests.lunchD]}
        ).mapValues{it.value.toLong()}
    }
    fun getTotalSessionCount(): Long = transaction{
        TutoringRequests.selectAll().count()
    }
    fun getSessionsByStatus(): Map<String, Long> = transaction{
        TutoringRequests
            .slice(TutoringRequests.status, TutoringRequests.id.count())
            .selectAll()
            .groupBy(TutoringRequests.status)
            .associate { row ->
                row[TutoringRequests.status] to row[TutoringRequests.id.count()]
            }
    }
    fun getDateRange(): Pair<java.time.LocalDate?, java.time.LocalDate?> = transaction {
        val minDate = TutoringRequests
            .slice(TutoringRequests.date.min())
            .selectAll()
            .firstOrNull()?.get(TutoringRequests.date.min())

        val maxDate = TutoringRequests
            .slice(TutoringRequests.date.max())
            .selectAll()
            .firstOrNull()?.get(TutoringRequests.date.max())

        minDate to maxDate
    }
}