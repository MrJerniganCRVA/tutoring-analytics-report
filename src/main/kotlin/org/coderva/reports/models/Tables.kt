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
    val r1Id = integer("R1Id").nullable()
    val r2Id = integer("R2Id").nullable()
    val rrId = integer("RRId").nullable()
    val r4Id = integer("R4Id").nullable()
    val r5Id = integer("R5Id").nullable()

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