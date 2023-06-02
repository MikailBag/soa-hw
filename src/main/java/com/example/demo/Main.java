package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.locks.LockSupport;

@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var app = new SpringApplication(Main.class);

        log.info("Starting server");
        app.setAdditionalProfiles("server");

        app.run(args);
        // sleep forever since we are server; out thread will be killed at the end of the
        // JVM shutdown sequence.
        while (true) {
            LockSupport.park();
        }
    }
}
