FROM openjdk:17

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} social-network.jar

ENTRYPOINT ["java", "-jar", "social-network.jar"]

EXPOSE 8080