## Kubernetes

If any changes are made to code, a push of the auction-service and bid-service need to be made to the repository (jxlok/pawn-auction-service:1.0 and jxlok/pawn-bid-service:1.0)
. These are referenced by our kubernetes deployments.

Firstly, package the project, 'mvn clean package'

### Build and push services to repo
1. In auction_service folder, run docker build -t jxlok/pawn-auction-service:1.0
2. Then, docker push jxlok/pawn-auction-service:1.0
3. Repeat steps 1 and 2 for bid-service

### Run Kubernetes Deployments, Services, Pods
1. Run minikube, 'minikube start' in a terminal
2. Check no existing pods exist with 'kubectl get pods', if existing run 'kubectl delete all --all' and 'kubectl delete pvc --all'
3. Go to kubernetes_pods directory, and 'run kubectl apply -f .'
4. Check pods are running, 'kubectl get pods' and ensure init-kafka is Completed
5. Post forward local machine port 8000->80 in nginx pod
   1. Get ID of nginx pod, 'kubectl get pods'
   2. In a new command prompt, execute 'kubectl post-forward <nginx_pod_id> 8000:80'

## Start Client
1. Go back to root folder
2. Execute 'java -jar ./client/target/auction-client-0.0.1.jar --userid=jingyi --server.port=8080'