# Build: Gradle + JDK 17 (matches jvmToolchain(17) in build.gradle.kts).
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle installDist --no-daemon

# Run: JRE only. Defaults to the HTTP server the RR Tutoring app calls.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/install/tutoring-analytics-report /app
# Keep the heap inside a small Railway container.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["/app/bin/tutoring-analytics-report", "serve"]
