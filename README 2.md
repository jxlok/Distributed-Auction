# Distributed_Systems_-COMP30220

## Test: bidservice works

1. In one terminal  window
```docker exec -it broker sh
  /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pawn.auction.bids --from-beginning
```
2. open another window
```
curl -X POST localhost:8083/bids -d '{"auctionId": 2, "offerPrice": 520, "userId": "jingyi"}' -H 'Content-Type: application/json'
```

## Test: The whole system

### Description

1. Bid-Service, Auction-Service, Kafka, Ngnix and MySql run in docker compose.
2. Bid-Service expose following endpoints via Ngnix:
    * POST http://localhost:8000/bids
3. Auction-Service exposes following endpoints via Ngnix:
   * GET http://localhost:8000/auctions
   * POST http://localhost:8000/auctions
4. Client(s) run outside Docker Compose network and on Host machines.
5. Client(s) send HTTP requests via Ngnix
6. Client(s) consume Kafka via exposed port 19092.

### Test

1. build the services: `mvn clean install`
2. run docker compose `docker compose up --build`
3. run built client jar file: `java -jar ./client/target/auction-client-0.0.1.jar --userId=jingyi --server.port=8080`
    * `--userId=xxx` decides the bidder name of this client
    * `--server.port=8080` has to be unique per client app, given that all clients will be run on the same machine, therefore ports must be unique.
4. Once client app is up and running, follow the instructions on the console. Available commands are:
    * `refresh`: showing all auction items in the system
    * `bid`: send a new bid for a given item, subsequent prompts will ask for following details:
      1. auction id
      2. new price
    * `create`:create a new item store in the database. follow the hint in console.



