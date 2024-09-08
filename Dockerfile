# Step 1: Use an OpenJDK 21 base image with Maven installed
FROM openjdk:21-slim AS build

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code at the same time
COPY pom.xml .
COPY src ./src

# Build the project and create the JAR file
RUN mvn clean install -DskipTests

# Step 2: Use the final image with only the JAR file
FROM openjdk:21-slim
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose port 8080
EXPOSE 10000

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
