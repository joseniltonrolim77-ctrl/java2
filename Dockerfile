FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml ./
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /usr/src/app

COPY --from=build /build/target/devshowcase-api.jar app.jar

ENV PORT=3577
EXPOSE 3577

CMD ["java", "-jar", "app.jar"]
