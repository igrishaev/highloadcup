FROM java:8

WORKDIR /

COPY target/uberjar/highloadcup.jar .

ENV SERVER_PORT 80
ENV SERVER_HOST "0.0.0.0"
ENV DB_URL "datomic:mem://highloadcup"
ENV ZIP_PATH "/tmp/data/data.zip"

EXPOSE 80

CMD ["java", "-jar", "highloadcup.jar"]
