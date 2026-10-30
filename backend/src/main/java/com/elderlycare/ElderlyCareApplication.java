package com.elderlycare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.elderlycare", "controller", "config", "service"})
public class ElderlyCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElderlyCareApplication.class, args);
    }
}
