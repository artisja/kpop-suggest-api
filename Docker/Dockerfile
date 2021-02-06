FROM openjdk:8-jre-alpine

# Copy war file
COPY kpopsuggest-0.0.1-SNAPSHOT.jar /kpopsuggest.war

# run the app
CMD ["/usr/bin/java", "-jar", "/kpopsuggest.war"]