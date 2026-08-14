package com.cinetest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RetoCpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetoCpBackendApplication.class, args);
    }

}
