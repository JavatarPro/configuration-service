FROM docker-dev.javatar.space/java8:1.2
# TODO add java8 to docker hub
WORKDIR /service
ENV JAVA_OPTS ""
ENV SERVICE_PARAMS ""
ADD configuration-service-app/target/configuration-service-app.jar /service/
CMD java-entrypoint configuration-service-app.jar