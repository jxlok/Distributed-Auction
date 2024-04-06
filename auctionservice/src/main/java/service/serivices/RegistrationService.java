package service.serivices;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;

public class RegistrationService {
    ArrayList<String> serviceURL = new ArrayList<>();

    public ArrayList<String> getServicesURLs() {
        return serviceURL;
    }

    public void register(String url) {
        serviceURL.add(url);
    }

}
