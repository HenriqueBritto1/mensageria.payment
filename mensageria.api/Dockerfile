# ============================================================
# Stage 1 – Build
# ============================================================

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml                        ./pom.xml
COPY mensageria.commons/pom.xml     mensageria.commons/pom.xml
COPY mensageria.api/pom.xml         mensageria.api/pom.xml
COPY mensageria.processor/pom.xml   mensageria.processor/pom.xml

RUN mvn dependency:go-offline -B --no-transfer-progress

COPY mensageria.commons/src    mensageria.commons/src
COPY mensageria.api/src        mensageria.api/src
COPY mensageria.processor/src  mensageria.processor/src

RUN mvn package -B --no-transfer-progress -DskipTests \
    -pl mensageria.commons,mensageria.api \
    -am

# ============================================================
# Stage 2 – Runtime
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app

COPY --from=build /workspace/mensageria.api/target/*.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]