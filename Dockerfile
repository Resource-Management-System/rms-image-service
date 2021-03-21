FROM openjdk:15
EXPOSE 5005

COPY target/imageService-*.jar /imageService.jar

ENTRYPOINT ["java", "-jar", "/imageService.jar"]
