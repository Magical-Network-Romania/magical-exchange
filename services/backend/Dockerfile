FROM maven:3.9-eclipse-temurin-21-noble AS build
WORKDIR /workspace
COPY services/backend/pom.xml services/backend/pom.xml
COPY services/backend/config services/backend/config
COPY services/backend/src services/backend/src
RUN mvn -f services/backend/pom.xml -DskipTests package

FROM eclipse-temurin:21.0.11_10-jre-noble
WORKDIR /app
COPY --from=build /workspace/services/backend/target/magical-exchange-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
