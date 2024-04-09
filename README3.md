#Testing Auction Service
1. Run: mvn clean package
2. Run: docker compose up --build
3. In a separate command prompt, run 
```
curl -X POST http://localhost:8000/bids -d "{\"auctionId\": 2, \"offerPrice\": 520, \"userId\": \"jingyi\"}" -H "Content-Type: application/json"
```

To note: If you want a fresh start, you must do docker compose down -v to reset sql volumes