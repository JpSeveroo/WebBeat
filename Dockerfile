FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache dos2unix

