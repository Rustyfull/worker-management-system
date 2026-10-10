FROM maven:3.9.9-eclipse-temurin AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY /src ./src

RUN mvn package

FROM eclipse-temurin:21-jre-alpine AS runner

COPY --from=builder /app/target/*.jar ./app.jar

EXPOSE 4003


ENTRYPOINT ["java", "-jar", "app.jar"]