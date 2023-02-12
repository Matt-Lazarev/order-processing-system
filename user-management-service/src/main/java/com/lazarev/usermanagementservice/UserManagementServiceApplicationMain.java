package com.lazarev.usermanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class UserManagementServiceApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(UserManagementServiceApplicationMain.class, args);
    }
}
