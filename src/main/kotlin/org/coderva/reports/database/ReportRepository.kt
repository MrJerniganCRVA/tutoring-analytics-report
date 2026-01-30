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
            .groupBy(Teacher.subject, TutoringRequests.date)
            .orderBy(TutoringRequests.date to SortOrder.ASC)
            .map {
                DepartmentTrend(
                    department = it[Teacher.subject],
                    date = it[TutoringRequests.date]
                    sessionsCount = it[TutoringRequests.id.count()]
                )
            }
             fun getSessionCountByTeacher(): Map<String, Long> = transaction {
        (Teachers innerJoin TutoringRequests)
            .slice(Teachers.firstName, Teachers.lastName, TutoringRequests.id.count())
            .selectAll()
            .groupBy(Teachers.id)
            .associate { 
                "${it[Teachers.firstName]} ${it[Teachers.lastName]}" to it[TutoringRequests.id.count()] 
            }
    }
    
    fun getTopStudentsByTeacher(teacherId: Int, limit: Int = 10): List<StudentSessionCount> = transaction {
        (Students innerJoin TutoringRequests)
            .slice(Students.firstName, Students.lastName, Students.id, TutoringRequests.id.count())
            .select { TutoringRequests.teacherId eq teacherId }
            .groupBy(Students.id)
            .orderBy(TutoringRequests.id.count() to SortOrder.DESC)
            .limit(limit)
            .map { 
                StudentSessionCount(
                    studentId = it[Students.id],
                    studentName = "${it[Students.firstName]} ${it[Students.lastName]}",
                    sessionCount = it[TutoringRequests.id.count()]
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
            .map { 
                StudentSessionCount(
                    studentId = it[Students.id],
                    studentName = "${it[Students.firstName]} ${it[Students.lastName]}",
                    sessionCount = it[TutoringRequests.id.count()]
                )
            }
    }
    
    fun getStudentsWithoutSessions(): List<Student> = transaction {
        Students
            .leftJoin(TutoringRequests)
            .slice(Students.id, Students.firstName, Students.lastName)
            .select { TutoringRequests.id.isNull() }
            .map { 
                Student(
                    id = it[Students.id],
                    firstName = it[Students.firstName],
                    lastName = it[Students.lastName]
                )
            }
    }
    
    
    fun getSessionsByDate(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Long> = transaction {
        TutoringRequests
            .slice(TutoringRequests.date, TutoringRequests.id.count())
            .select { TutoringRequests.date.between(startDate, endDate) }
            .groupBy(TutoringRequests.date)
            .associate { 
                it[TutoringRequests.date] to it[TutoringRequests.id.count()] 
            }
    }
    
    fun getMostRequestedSubjects(limit: Int = 10): List<SubjectCount> = transaction {
        (Teachers innerJoin TutoringRequests)
            .slice(Teachers.subject, TutoringRequests.id.count())
            .selectAll()
            .groupBy(Teachers.subject)
            .orderBy(TutoringRequests.id.count() to SortOrder.DESC)
            .limit(limit)
            .map { 
                SubjectCount(
                    subject = it[Teachers.subject],
                    count = it[TutoringRequests.id.count()]
                )
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
                gradeLevel to row[TutoringRequests.id.count()]
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { it.value.sum() }
    }
    }

}

data class DepartmentTrend(
    val department: String,
    val date: LocalDate
    val sessionCount: Long
)

data class StudentSessionCount(
    val studentId: Int,
    val studentName: String,
    val sessionsCount: Long
)

data class SubjectCount(
    val subject: String,
    val count: Long
)