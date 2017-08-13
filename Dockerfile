FROM java:8

WORKDIR /

COPY target/uberjar/highloadcup.jar .

CMD ["java", "-jar", "highloadcup.jar"]
