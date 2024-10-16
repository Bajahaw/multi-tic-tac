# Step 1: Use a Liberica Lite base image with Maven
FROM bellsoft/liberica-openjdk-alpine:21 AS build

# Install Maven
RUN apk update && apk add maven

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code at the same time
COPY pom.xml .
COPY src ./src

# Build the project and create the JAR file
RUN mvn clean package

# Step 2: Use a Liberica Lite base image for the final stage
FROM bellsoft/liberica-runtime-container:jre-21-slim-glibc AS runtime
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose port 10000
EXPOSE 10000

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]