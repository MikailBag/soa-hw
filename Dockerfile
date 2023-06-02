FROM eclipse-temurin:19-jdk-alpine
VOLUME /tmp
WORKDIR /app
ADD /demo-0.0.1-SNAPSHOT.tar /app
ENTRYPOINT ["java", "-cp", "/app/demo-0.0.1-SNAPSHOT/lib/*", "--enable-preview", "--add-modules", "jdk.incubator.concurrent", "com.example.demo.Main"]