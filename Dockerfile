FROM openjdk:8-alpine
COPY build/libs/data-upload-api-*.jar data-upload-api.jar
CMD java -jar data-upload-api.jar