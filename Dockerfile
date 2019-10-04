FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /workspace
WORKDIR /workspace

RUN gradle build --no-daemon

FROM adoptopenjdk/openjdk11:debian-slim

RUN mkdir /app

# Kaniko does not like a wild card for the fat jar
COPY --from=builder /workspace/build/libs/ampwdgen-1.0.0-fat.jar /app/ampwdgen.jar

EXPOSE 8888

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/ampwdgen.jar"]
