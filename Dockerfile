FROM amazoncorretto:21-alpine

# Set the working directory
WORKDIR /app

# Copy build jar from build context
COPY build/libs/challenge-*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]