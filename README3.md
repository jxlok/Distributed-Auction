## Kubernetes

If any changes are made to code, a push of the auction-service and bid-service need to be made to the repository (jxlok/pawn-auction-service:1.0 and jxlok/pawn-bid-service:1.0)
. These are referenced by our kubernetes deployments.


### Build and push services to repo if changes has been made
1. Firstly, package the project, 'mvn clean package'
2. In auctionservice folder, run docker build -t jxlok/pawn-auction-service:1.0 .
3. Then, docker push jxlok/pawn-auction-service:1.0
4. Repeat steps 1 and 2 for bidservice

### Run Kubernetes Deployments, Services, Pods
1. Run minikube, 'minikube start' in a terminal
2. Check no existing pods exist with 'kubectl get pods', if existing run 'kubectl delete all --all' and 'kubectl delete pvc --all'
3. Go to kubernetes_pods directory, and run 'kubectl apply -f .' 
4. Check pods are Running using 'kubectl get pods' and ensure init-kafka is Completed (this takes me about 2 mins before everything is up running)
5. Post forward local machine port 8000->80 in nginx pod
   1. Get ID of nginx pod, 'kubectl get pods'
   2. In a new command prompt, execute 'kubectl port-forward <nginx_pod_id> 8000:80'
   3. Do the same for kafka 9092->9092

## Start Client
1. Go back to root folder
2. Execute 'java -jar ./client/target/auction-client-0.0.1.jar --userId=jingyi --server.port=8080'

#### To note: If changes have been made to the configmaps in nginx-config.yaml or mysql-init-scripts.yaml, removal of the specific configmaps must be done with the following commands. 
#### - kubectl get configmaps
#### - kubectl delete configmap mysql-init-scripts
#### - kubectl delete configmap nginx-config