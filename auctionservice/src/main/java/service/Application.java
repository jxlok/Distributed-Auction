package service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application  {
    public static void main(String[] args)  {
        SpringApplication.run(Application.class, args);
    }
}
