plugins {
    kotlin("jvm") version "1.9.22"
    application
}
group = "org.coderva"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies{
    implementation(kotlin("stdlib"))

    //postgres
    implementation("org.postgresql:postgresql:42.7.1")

    //database access
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.45.0")

    //apache for excel
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    //apache pdf box
    implementation("org.apache.pdfbox:pdfbox:3.0.1")

    //logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    //coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

application{
    mainClass.set("org.coderva.reports.MainKt")
}

kotlin{
    jvmToolchain(17)
}