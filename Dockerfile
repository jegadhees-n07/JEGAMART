FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B dependency:go-offline

COPY src src

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/jd-mart-1.0.0.jar app.jar

ENV SPRING_PROFILES_ACTIVE=render
ENV SPRING_DATASOURCE_URL=jdbc:h2:mem:jdmart;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
ENV SPRING_DATASOURCE_USERNAME=sa
ENV SPRING_DATASOURCE_PASSWORD=
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
