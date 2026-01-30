FROM openjdk:8-jdk-alpine
LABEL authors="panpan"

COPY ./target/*.jar /demo.jar
ENTRYPOINT ["java","-jar","/demo.jar"]