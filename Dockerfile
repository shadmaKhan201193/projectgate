FROM openjdk:17-jdk-slim
COPY /build/libs/gatewayservice-0.0.1.jar usr/src/gatewayservice-0.0.1.jar
EXPOSE 8183
CMD ["java","-jar","usr/src/gatewayservice-0.0.1.jar"]
