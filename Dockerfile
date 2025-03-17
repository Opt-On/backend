# Use a Maven image to build the application
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (to leverage Docker caching)
COPY spring-boot/pom.xml .

RUN mvn dependency:go-offline

# COPY spring-boot/ .

COPY .env .
COPY firebase-key.json .

# Copy the source code
COPY spring-boot/src ./src

# Build the application
RUN mvn clean package

# Use a minimal JDK image to run the app
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built JAR file from the build stage
# COPY --from=build /app/target/opton.jar app.jar
# COPY /app/target/opton.jar app.jar

# Expose the app's port (default Spring Boot port is 8080)
# EXPOSE 8081
EXPOSE 8080
# RUN chmod +x app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "/app/target/opton.jar"]

# ENTRYPOINT ["sh", "-c", "echo 'Container is running...'; tail -f /dev/null"]
