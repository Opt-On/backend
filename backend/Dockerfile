FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y maven

COPY backend/ .

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]
