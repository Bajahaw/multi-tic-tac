FROM openjdk:21-jdk
LABEL authors="radhi"
COPY target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]