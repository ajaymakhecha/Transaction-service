# Use the official OpenJDK image as the base image
FROM eclipse-temurin


# Set the working directory in the container
WORKDIR /app
# First ensure target directory exists
RUN mkdir -p /target
# Copy the JAR file from the Maven build
COPY target/Transaction-Service-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]