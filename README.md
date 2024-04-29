# Team name: Pawn
## Members:
| Student number | Name         | Email                      |
|----------------|--------------|----------------------------|
| 19204584   | Jingyi Cui   | jingyi.cui1@ucdconnect.ie  |
| 19203637          | Haocheng Sun | haocheng.sun@ucdconnect.ie |
| 20366363       | Jason Lok    | jason.lok@ucdconnect.ie    |

## Project Summary
Our project involves the creation of an auction system allowing for clients 
to place bids, sell items and view the latest bids on all the items in the system.
The three main components are:
- BidService: 
  - Validates bid offers and adds a timestamp to them annotating time of receipt
- AuctionService: 
  - Deals with new auction item, new bids and validates expiry times of items in
  the database. 
  - If any changes are to occur in the database, informs Clients of the change.
- Client: 
  - Views the most up to date snapshot of auction items and their bids. 
  - Places bids on existing items and puts items up for sale.
  - Receives all bids that appear in the system regardless if triggers updates
- Nginx: Used for load balancing on BidService and AuctionService
- Kafka Message Broker: Kafka Topics used for communication of services
- Kubernetes: Used for container orchestration in minikube.

## Running the code
If any changes are made to code, a push of the auction-service and bid-service need to be made to the repository (jxlok/pawn-auction-service:1.0 and jxlok/pawn-bid-service:1.0)
. These are referenced by our kubernetes deployments.

#### Firstly, package the project using 'mvn clean package'

### Build and push services to repo if changes has been made
2. In auctionservice folder, run docker build -t jxlok/pawn-auction-service:1.0 .
3. Then, docker push jxlok/pawn-auction-service:1.0
4. Repeat steps 1 and 2 for bidservice (jxlok/pawn-bid-service:1.0)

### Run Kubernetes Deployments, Services, Pods
1. Run minikube, 'minikube start' in a terminal
2. Check no existing pods exist with 'kubectl get pods'
   1. if existing found, run
   ```
   kubectl delete all --all
   kubectl delete pvc --all
   ```
3. Go to kubernetes_pods directory, and run 
```
kubectl apply -f .
```
4. Check pods are Running using 
```
'kubectl get pods' 
```
and ensure init-kafka is Completed and every other container is running. This can be slow due to the dependencies that exist between our containers.

5. Post forward local machine port 8000->80 in nginx pod
    1. Get ID of nginx pod
   ```
   'kubectl get pods'
   ```
   2. In a new command prompt, execute 
   ```
   'kubectl port-forward <nginx_pod_id> 8000:80'
   ```
   3. Do the same for kafka (broker pod) using port mapping: 9092->9092

### Start Client
1. Go back to root folder
2. Execute 
```
java -jar ./client/target/auction-client-0.0.1.jar --userId=jingyi --server.port=8080
```

#### To note: If changes have been made to the configmaps in nginx-config.yaml or mysql-init-scripts.yaml, removal of the specific configmaps must be done with the following commands.
   ```
kubectl get configmaps
kubectl delete configmap mysql-init-scripts
kubectl delete configmap nginx-config
   ```

## Report + Video links
- [Auction Report (PDF)](./Distributed_Report_Pawn.pdf)
- [Auction Video](./Pawn-DistributedAuctionDemo.mp4)