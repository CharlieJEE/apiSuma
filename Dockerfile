FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} apiSuma-1.0.jar
ENTRYPOINT [ "java","-jar","/apiSuma-1.0.jar" ]