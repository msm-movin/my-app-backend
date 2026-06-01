# Java 23 का बेस इमेज (क्योंकि तुम्हारे build.gradle में Java 23 है)
FROM eclipse-temurin:23-jdk

# वर्किंग डायरेक्टरी सेट करें
WORKDIR /app

# प्रोजेक्ट का सारा कोड कॉपी करें
COPY . .

# gradlew को रन करने की परमिशन दें
RUN chmod +x gradlew

# प्रोजेक्ट को बिल्ड करें (बिना टेस्ट रन किए)
RUN ./gradlew clean build -x test

# पोर्ट 8080 एक्सपोज़ करें
EXPOSE 8080

# स्प्रिंग बूट ऐप को स्टार्ट करें
CMD ["java", "-jar", "build/libs/mycrud-0.0.1-SNAPSHOT.jar"]