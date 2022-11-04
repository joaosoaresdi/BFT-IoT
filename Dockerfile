#FROM maven
FROM arm64v8/openjdk

COPY ./target/classes/ /app
COPY ./config /app/config
COPY ./lib /app/lib

#COPY . /app
WORKDIR /app
#RUN ["mvn", "dependency:resolve"]
#RUN ["mvn", "verify"]
#RUN ["mvn", "package"]

ENTRYPOINT ["java", "-Djava.security.properties=/app/config/java.security", "-Dlogback.configurationFile=/app/config/logback.xml", "-cp", ".:./lib/*"]
CMD ["bft_iot.proxy.BFT_Broker_Client", "0", "8080"]
#COPY out/artifacts/BFT_IoT_jar/BFT-IoT.jar /app
#CMD ["java", "-Djava.security.properties=./config/java.security", "-Dlogback.configurationFile=./config/logback.xml", "-cp", ".:lib/*", "-jar", "target/BFT-IoT-1.0.jar", "bft_iot.proxy.BFT_Broker_Proxy", "0"]

#java -jar out/artifacts/BFT_IoT_jar/BFT-IoT.jar -Djava.security.properties=./config/java.security -Dlogback.configurationFile=./config/logback.xml bft_iot.proxy.BFT_Broker_Proxy 0

#java -Djava.security.properties=./config/java.security -Dlogback.configurationFile=./config/logback.xml -jar ./out/artifacts/BFT_IoT_jar/BFT-IoT.jar bft_iot.proxy.BFT_Broker_Proxy 0
