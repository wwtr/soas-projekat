FROM eclipse-temurin:17-jre-alpine
EXPOSE 8000
ADD target/currency-exchange-1.0.0.jar app/currency-exchange.jar
ENTRYPOINT [ "java", "-jar", "app/currency-exchange.jar" ]
