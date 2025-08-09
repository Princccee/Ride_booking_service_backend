# ---------- Stage 1: build ----------
FROM gradle:8.4-jdk17 AS builder
WORKDIR /home/gradle/project

# copy sources and gradle wrapper
COPY --chown=gradle:gradle . .

# make sure gradlew is executable
RUN chmod +x gradlew

# build the bootJar (no daemon)
RUN ./gradlew clean bootJar --no-daemon

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=build/libs/*.jar
WORKDIR /app

# non-root user (optional)
RUN addgroup --system app && adduser --system --ingroup app app
USER app

# copy jar from builder
COPY --from=builder /home/gradle/project/${JAR_FILE} app.jar

# expose port (only informational)
EXPOSE 8080

# recommended: pass JVM options via environment variable
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
