FROM maven:3.9.3-eclipse-temurin-20 as build

WORKDIR /home/raw

## (TODO) comment/uncomment as soon as public repositories
ADD ./ /home/raw/raw-jena/
ADD ./temp/sage-jena/ /home/raw/sage-jena/
##RUN git clone https://github.com/Chat-Wane/raw-jena \
##    && git clone https://github.com/Chat-Wane/sage-jena

WORKDIR /home/raw/sage-jena

RUN mvn install -Dmaven.test.skip=true

WORKDIR /home/raw/raw-jena

RUN mvn package -Dmaven.test.skip=true

WORKDIR /home/raw/raw-jena/raw-jena-ui

RUN apt-get update && apt-get -y install npm && npm install



FROM amd64/eclipse-temurin:20.0.1_9-jre-alpine

WORKDIR /home/raw
COPY --from=build /home/raw/raw-jena/raw-jena-ui ./

RUN addgroup -S raw \
    && adduser -G raw -S raw

RUN chown -R raw:raw /home/raw \
    && chmod -R 755 /home/raw 

USER raw

COPY --from=build /home/raw/raw-jena/raw-jena-module/target/raw-jena-module-0.0.1.jar ./

ENTRYPOINT ["java", "-jar", "raw-jena-module-0.0.1.jar"]

