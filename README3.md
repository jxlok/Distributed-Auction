#Testing Auction Service
1. Run: mvn clean package
2. Run: docker compose up --build
3. In a separate command prompt, run 
```
curl -X POST http://localhost:8000/bids -d "{\"auctionId\": 2, \"offerPrice\": 520, \"userId\": \"jingyi\"}" -H "Content-Type: application/json"
```
4. Validate in sql database from the sql container terminal in Docker Desktop with commands:
- mysql -u root -p
- type password: root_password
- use auction_db
- select * from current_bids

Confirm changes have occurred
5. Also check that the changed offer is posted to kafka (updates) topic:
- Open broker container terminal in docker desktop
- run
```
  /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic pawn.auction.updates --from-beginning
```

To note: If you want a fresh start, you must do docker compose down -v to reset sql volumes