# BFT-IoT Proxy

This is a BFT proxy server for IoT, designed as a substitute for Fiware's Orion Context Broker.

It offers a similar REST interface (not all implemented) for creating, querying, updating and subscribing to context 
information.

It used the BFT-SMART, the byzantine fault-tolerant state machine replication library as its consensus primitive.

## BFT-IoT Proxy Server

Provides the backend (a.k.a., replica) for the Context Broker.
It forwards operations to the respective Orion Context Broker, only after reaching agreement on the received values.

Run using:

`java -Djava.security.properties=./config/java.security -Dlogback.configurationFile=./config/logback.xml -jar ./out/artifacts/BFT_IoT_jar/BFT-IoT.jar bft_iot.proxy.BFT_Broker_Proxy <id>`

## BFT-IoT Proxy Client
Provides the frontend (a.k.a., REST interface) for the BFT-IoT Proxy Server.
It provides a REST interface compatible with Orion Context Broker, and forwards operations to the respective BFT-IoT Proxy Server.

Run using:

`java -Djava.security.properties=./config/java.security -Dlogback.configurationFile=./config/logback.xml -jar ./out/artifacts/BFT_IoT_jar/BFT-IoT.jar bft_iot.proxy.BFT_Broker_Client <id> <port>`


## Docker and Docker-Compose

Alternative you can build a Docker image file using the respective `Dockerfile`.

And instantiate the necessary containers.

Alternatively you can use the `docker-compose.yml` file to instanciate the necessary services for running an `f=1` configuration setup.

## Updates and Modifications

1. Create the corresponding Sensor Entity and Entity Update classes in bft_iot.json.entities
2. Update BFT_Broker_Client to process corresponding entities
3. Update BFT_Broker_Proxy to process corresponding entities and operations
4. `git add .`
5. git commit -m "commit message"
6. git push
7. On each RaspberryPi nodes
   1. git pull
   2. docker build -t bft-iot_1 .
   3. docker-compose up