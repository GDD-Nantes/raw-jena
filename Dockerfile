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

RUN mvn install -Dmaven.test.skip=true
RUN mvn dependency:copy-dependencies
RUN mvn package -Dmaven.test.skip=true

WORKDIR /home/raw/raw-jena/raw-jena-ui

RUN apt-get update && apt-get -y install npm && npm install --production



FROM amd64/eclipse-temurin:20.0.1_9-jre-alpine

WORKDIR /home/raw
COPY --from=build /home/raw/raw-jena/raw-jena-ui ./raw-jena-ui

WORKDIR /home/raw/raw-jena

RUN addgroup -S raw \
    && adduser -G raw -S raw

#RUN chown -R raw:raw /home/raw \
#    && chmod -R 755 /home/raw 

USER raw

COPY --from=build /home/raw/raw-jena/raw-jena-module/target/*.jar ./

ENTRYPOINT ["java", "-cp", "./", "-jar", "raw-jena-module-0.0.1.jar", "--ui", "/home/raw/raw-jena-ui", "--database", "/database/"]

## since some arguments are already set, users simply need to `docker run`.
## The container awaits a volume containing the data into `/database`.
## The port can be modified using `--port` but do not forget to change it
## in the docker command afterwards.
## docker run -p 3330:3330 -v path/to/datasets/WDBench/:/database raw-jena:latest
