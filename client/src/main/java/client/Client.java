//package Client;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import okhttp3.*;
//import service.core.Application;
//import service.core.ClientInfo;
//
//import java.io.IOException;
//import java.util.Objects;
//
//public class Client {
//    private final OkHttpClient httpClient = new OkHttpClient();
//
//     public String sendPOST(String url, ClientInfo clientInfo) throws IOException {
//        //prepare request
//        var objectMapper = new ObjectMapper();
//        String requestBody = objectMapper.writeValueAsString(clientInfo);
//        //prepare request body
//        RequestBody body = RequestBody.create(
//                requestBody,
//                MediaType.parse("application/json"));
//        ;
//        //Create HTTP request
//        Request request = new Request.Builder()
//                .url(url)
//                .post(body)
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            // Get url in header
//            //System.out.println(response.headers().get("Location"));
//            return response.headers().get("Location");
//        }
//
//    }
//
//     public Application sendGET(String url) throws IOException {
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//
//        try (Response response = httpClient.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            //Change Response to application object: byte array -> string -> jason -> application(object)
//            ObjectMapper objectMapper = new ObjectMapper();
//            try {
//                return objectMapper.readValue(Objects.requireNonNull(response.body()).string(), Application.class);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        return null;
//    }
//
//
//}
