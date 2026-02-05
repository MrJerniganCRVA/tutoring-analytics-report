package org.coderva.reports.models
import java.time.LocalDate

data class Student(
    val id: Int,
    val firstName: String,
    val lastName: String
) {
    val fullName: String
        get() = "$firstName $lastName"

    fun getGradeLevel(currentSchoolYear: Int):String {
        val idString = id.toString()
        val startYear = idString.take(2).toInt()
        val yearsInSchool = currentSchoolYear - startYear
        return when(yearsInSchool) {
            0 -> "9th Grade"
            1 -> "10th Grade"
            2 -> "11th Grade"
            3 -> "12th Grade"
            else -> "Other"
        }
    }
}

data class DepartmentTrend(
    val department: String,
    val date: LocalDate,
    val sessionCount: Long
)

data class StudentSessionCount(
    val studentId: Int,
    val studentName: String,
    val sessionCount: Long
)

data class SubjectCount(
    val subject: String,
    val count: Long
)