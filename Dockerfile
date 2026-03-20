FROM eclipse-temurin:17-jre

ENV TZ=Asia/Shanghai
RUN apt-get update \
    && apt-get install -y --no-install-recommends tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime \
    && echo $TZ > /etc/timezone \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY ./app/target/demo.jar /app/demo.jar
ENTRYPOINT ["java","-jar","/app/demo.jar"]
