FROM eclipse-temurin:17-jre-alpine
EXPOSE 8200
ADD target/bank-account-1.0.0.jar app/bank-account.jar
ENTRYPOINT [ "java", "-jar", "app/bank-account.jar" ]
