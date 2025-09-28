# ===== build =====
FROM maven:3-amazoncorretto-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -DskipTests package

# ===== runtime =====
FROM amazoncorretto:21-alpine
# OS を最新化（脆弱性パッチを取り込む）
RUN apk update && apk upgrade --no-cache \
 && apk add --no-cache curl
WORKDIR /app
COPY --from=build /workspace/target/MatchScheduler-0.0.1-SNAPSHOT.jar /app/demo.jar
EXPOSE 8080
USER 10001
ENTRYPOINT ["java","-jar","/app/demo.jar"]