FROM eclipse-temurin:17-jre-alpine
EXPOSE 8300
ADD target/crypto-wallet-1.0.0.jar app/crypto-wallet.jar
ENTRYPOINT [ "java", "-jar", "app/crypto-wallet.jar" ]
