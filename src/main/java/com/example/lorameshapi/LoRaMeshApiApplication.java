package com.example.lorameshapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@AllArgsConstructor
@RestController
public class LoRaMeshApiApplication {
    private final Logger logger = LoggerFactory.getLogger(LoRaMeshApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LoRaMeshApiApplication.class, args);

        ScheduledExecutorService exe = new ScheduledThreadPoolExecutor(1);
        exe.scheduleAtFixedRate(System::currentTimeMillis, 0, 1, TimeUnit.HOURS);
    }

    @GetMapping
    public String helloWorld(HttpServletRequest request) {
        logger.info("HELLO WORLD REQUEST: " + request.getRequestURI());
        logger.info("Host: " + request.getRemoteHost());
        logger.info("Port: " + request.getRemotePort());
        logger.info("Address: " + request.getRemoteAddr());
        logger.info("User: " + request.getRemoteUser());
        return "Version 2";
    }

}
