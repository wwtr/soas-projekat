FROM eclipse-temurin:17-jre-alpine
EXPOSE 8400
ADD target/crypto-exchange-1.0.0.jar app/crypto-exchange.jar
ENTRYPOINT [ "java", "-jar", "app/crypto-exchange.jar" ]
