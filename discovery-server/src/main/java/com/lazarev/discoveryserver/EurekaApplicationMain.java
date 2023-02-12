package com.lazarev.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplicationMain.class, args);
    }
}
