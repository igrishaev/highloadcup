FROM frolvlad/alpine-oraclejdk8

ENV SERVER_PORT 80
ENV SERVER_HOST "0.0.0.0"
ENV DB_URL "datomic:mem://highloadcup"
ENV WARMUP_RATIO 1000
ENV ZIP_PATH "/tmp/data/data.zip"

EXPOSE 80

WORKDIR /
COPY target/uberjar/highloadcup.jar .

CMD ["java", "-server", "-da", "-dsa", "-Xmx4g", "-Xms4g", "-XX:+UseCompressedOops", "-XX:+DoEscapeAnalysis", "-XX:+UseConcMarkSweepGC", "-jar", "highloadcup.jar"]
