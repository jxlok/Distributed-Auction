//there is no need for this code, Just leave here for some reference if you guys want to copy someting
//import Client.Client;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//import service.core.Application;
//import service.core.ClientInfo;
//import service.core.Quotation;
//
//import java.io.IOException;
//import java.net.URL;
//import java.text.NumberFormat;
//import java.time.Instant;
//import java.util.ArrayList;
//
//public class Main {
//
//
//    //clientInfo -> POST -> get url -> GET -> get application in body -> print quotations
//    public static void main(String[] args) {
//        ArrayList<Application> applications = new ArrayList<>();
//        try {
//            Client client = new Client();
//            for (ClientInfo info : clients) {
//                String url = client.sendPOST("http://localhost:8083/applications", info);
//                System.out.println("Created application for client: " + info.name + ", URL: " + url);
//                System.out.println("Getting application from broker for client: " + info.name);
//                Application app = client.sendGET(url);
//                System.out.println("Successfully received application for client: " + info.name);
//                if(app !=null){
//                    applications.add(app);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        for (Application app : applications) {
//            displayProfile(app.info);
//
//            // Retrieve quotations from the broker and display them...
//            for(Quotation quotation : app.quotations) {
//                displayQuotation(quotation);
//            }
//
//            // Print a couple of lines between each client
//            System.out.println("\n");
//        }
//
//    }
//
//    public static void displayProfile(ClientInfo info) {
//        System.out.println("|=================================================================================================================|");
//        System.out.println("|                                     |                                     |                                     |");
//        System.out.println(
//                "| Name: " + String.format("%1$-29s", info.name) +
//                        " | Gender: " + String.format("%1$-27s", (info.gender==ClientInfo.MALE?"Male":"Female")) +
//                        " | Age: " + String.format("%1$-30s", info.age)+" |");
//        System.out.println(
//                "| Weight/Height: " + String.format("%1$-20s", info.weight+"kg/"+info.height+"m") +
//                        " | Smoker: " + String.format("%1$-27s", info.smoker?"YES":"NO") +
//                        " | Medical Problems: " + String.format("%1$-17s", info.medicalIssues?"YES":"NO")+" |");
//        System.out.println("|                                     |                                     |                                     |");
//        System.out.println("|=================================================================================================================|");
//    }
//
//
//    public static void displayQuotation(Quotation quotation) {
//        System.out.println(
//                "| Company: " + String.format("%1$-26s", quotation.company) +
//                        " | Reference: " + String.format("%1$-24s", quotation.reference) +
//                        " | Price: " + String.format("%1$-28s", NumberFormat.getCurrencyInstance().format(quotation.price))+" |");
//        System.out.println("|=================================================================================================================|");
//    }
//
//
//
//
//   private static Application createApplication(ClientInfo info) {
//        return new Application(info);
//    }
//
//    public static final ClientInfo[] clients = {
//            new ClientInfo("Niki Collier", ClientInfo.FEMALE, 49, 1.5494, 80, false, false),
//            new ClientInfo("Old Geeza", ClientInfo.MALE, 65, 1.6, 100, true, true),
//            new ClientInfo("Hannah Montana", ClientInfo.FEMALE, 21, 1.78, 65, false, false),
//            new ClientInfo("Rem Collier", ClientInfo.MALE, 49, 1.8, 120, false, true),
//            new ClientInfo("Jim Quinn", ClientInfo.MALE, 55, 1.9, 75, true, false),
//            new ClientInfo("Donald Duck", ClientInfo.MALE, 35, 0.45, 1.6, false, false)
//    };
//}
//
//
////Another method implementing client: use spring RestTemplate to sent request
////The data parse process will be handled by springboot
////        for (ClientInfo info : clients) {
////                working client method1: use spring RestTemplate to sent request
////            RestTemplate restClient = new RestTemplate();
////            ResponseEntity<Application> response = restClient.postForEntity("http://localhost:8083/applications", createApplication(info), Application.class);
////            String url = response.getHeaders().getLocation().toString();
////            applications.add(restClient.getForEntity(url, Application.class).getBody());
////
////                }