FROM maven:3-amazoncorretto-17 as build
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests

FROM amazoncorretto:17-alpine
COPY --from=build target/sa-identity-server-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]