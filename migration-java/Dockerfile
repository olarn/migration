FROM nexus.tmbbank.local:60014/gradle/gradle-openjdk21:8.11.1
USER 0
EXPOSE 8080
COPY ./build/libs/app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]