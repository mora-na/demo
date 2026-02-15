FROM openjdk:8-jdk-alpine
LABEL authors="panpan"

COPY ./app/target/*.jar /demo.jar
ENTRYPOINT ["java","-jar","/demo.jar"]
