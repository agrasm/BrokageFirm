package com.mehmet.brokagefirm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mehmet"})
public class BrokageFirmApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrokageFirmApplication.class, args);
    }

}