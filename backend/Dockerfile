# --- Build stage: compile and package with the Maven wrapper (no global Maven needed) ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Cache dependencies first for faster rebuilds.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

# Build the application jar (skip tests in the image build; tests run in CI).
COPY src/ src/
RUN ./mvnw -q -DskipTests package

# --- Run stage: slim JRE with just the jar ---
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# SQLite database file lives in a volume-friendly location.
ENV APP_API_TOKEN=dev-hr-token
ENV APP_CORS_ORIGINS=http://localhost:4200

COPY --from=build /app/target/salary-management-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
