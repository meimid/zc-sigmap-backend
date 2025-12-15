FROM eclipse-temurin:17-jdk AS builder

RUN apk update && apk add maven curl

WORKDIR /app

COPY sigmap-common ./sigmap-common
COPY sigmap-migrations ./sigmap-migrations
COPY sigmap-params ./sigmap-params
COPY sigmap-agpm ./sigmap-agpm
COPY sigmap-ppm ./sigmap-ppm
COPY sigmap-paa ./sigmap-paa
COPY sigmap-dac ./sigmap-dac
COPY sigmap-auth ./sigmap-auth
COPY sigmap-dac ./sigmap-dac
COPY sigmap-webmaster ./sigmap-webmaster
COPY sigmap-website-auth ./sigmap-website-auth
COPY sigmap-eclaircissement ./sigmap-eclaircissement

COPY pom.xml .

RUN mvn clean package -B -DskipTests

FROM eclipse-temurin:17-jre-slim AS runner

WORKDIR /app

COPY --from=builder /app/sigmap-ppm/target/sigmap-ppm*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
