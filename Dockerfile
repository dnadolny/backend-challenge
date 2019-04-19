FROM openjdk:8-jre-alpine
EXPOSE 8080
COPY run_app.sh target/scala-2.11/ada-backend-challenge-assembly-0.1.0-SNAPSHOT.jar ./
ENTRYPOINT ["./run_app.sh"]
