FROM java:8

WORKDIR /

COPY target/uberjar/highloadcup.jar .

ENV SERVER_PORT 80
ENV ZIP_PATH /tmp/data/data.zip

CMD ["java", "-jar", "highloadcup.jar"]
