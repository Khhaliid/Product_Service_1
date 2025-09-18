# Stage 1: Build
FROM maven:3.9.9-amazoncorretto-21-debian AS build
WORKDIR /app

# Kopiera pom och src
COPY pom.xml ./
COPY src ./src

# Dummy secret för build
ENV JWT_SECRET=dummysecret
ENV JWT_EXPIRATION=3600000
ENV BACKEND_PORT=8080

# Bygg jar
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM amazoncorretto:21-alpine
WORKDIR /app

# Uppdatera paket och installera curl (valfritt)
RUN apk update && apk upgrade --no-cache && apk add curl

# Sätt fallback JWT_SECRET och BACKEND_PORT
ENV JWT_SECRET=dummysecret
ENV JWT_EXPIRATION=3600000
ENV BACKEND_PORT=8080

# Kopiera jar från build
COPY --from=build /app/target/*.jar app.jar

# Skapa uppladdningsmapp och ge rätt ägarskap
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /app/uploads \
    && chown -R appuser:appgroup /app/uploads

# Skapa icke-root-användare
USER appuser

# Exponera port
EXPOSE 8080

# Starta applikationen
CMD ["java", "-jar", "/app/app.jar"]
