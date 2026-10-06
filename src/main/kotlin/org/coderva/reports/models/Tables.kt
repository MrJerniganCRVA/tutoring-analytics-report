package org.coderva.reports.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime


object Teachers : Table("\"Teachers\""){
    val id = integer("id")
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val email = varchar("email",255)
    val subject = varchar("subject",100)
    
    override val primaryKey = PrimaryKey(id)
} 
object Students: Table("\"Students\""){
    val id = integer("id")
    val firstName = varchar("first_name",100)
    val lastName = varchar("last_name", 100)
    // Class assignments (R1/R2/RR/R4/R5) moved to the app's Enrollments join
    // table; the old R1Id..R5Id columns no longer exist on Students.

    override val primaryKey = PrimaryKey(id)
    

}
object TutoringRequests : Table("\"TutoringRequests\"") {
    val id = integer("id").autoIncrement()
    val studentId = integer("StudentId") references Students.id
    val teacherId = integer("TeacherId") references Teachers.id
    val date = date("date")
    val lunchA = bool("lunchA")
    val lunchB = bool("lunchB")
    val lunchC = bool("lunchC")
    val lunchD = bool("lunchD")
    val status = varchar("status", 50).default("active")

    override val primaryKey = PrimaryKey(id)

}