FROM eclipse-temurin:17-jdk-alpine AS builder

RUN apk update && apk add maven curl

WORKDIR /app

COPY sigmap-common ./sigmap-common
COPY sigmap-migrations ./sigmap-migrations
COPY sigmap-params ./sigmap-params
COPY sigmap-agpm ./sigmap-agpm
COPY sigmap-ppm ./sigmap-ppm
COPY sigmap-paa ./sigmap-paa
COPY sigmap-auth ./sigmap-auth
COPY sigmap-dac ./sigmap-dac
COPY sigmap-webmaster ./sigmap-webmaster

COPY pom.xml .

RUN mvn clean package -B -DskipTests

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app


RUN apt-get update && apt-get install -y \
    fontconfig \
    ttf-mscorefonts-installer \
    fonts-dejavu \
 && rm -rf /var/lib/apt/lists/*

COPY sigmap-params/src/main/resources/fonts/tahoma.ttf /usr/share/fonts/truetype/tahoma.ttf
COPY sigmap-params/src/main/resources/fonts/tahomabd.ttf /usr/share/fonts/truetype/tahomabd.ttf

RUN fc-cache -f -v

COPY --from=builder /app/sigmap-params/target/sigmap-params*.jar /app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
