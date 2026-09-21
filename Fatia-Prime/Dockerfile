FROM eclipse-temurin:26-jdk

WORKDIR /app

COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew clean build --no-daemon

EXPOSE 8080

CMD ["java", "-jar", "build/libs/Fatia-Prime-0.0.1-SNAPSHOT.jar"]
