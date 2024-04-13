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


