FROM openjdk:8-alpine

WORKDIR /service
ENV JAVA_OPTS ""
ENV SERVICE_PARAMS ""
ADD configuration-service-app/target/configuration-service-app.jar /service/
CMD java $JAVA_OPTS -jar configuration-service-app.jar $SERVICE_PARAMS

