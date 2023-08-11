package com.example.lorameshapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Configuration
public class DateConfig {
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss.SSSS Z");


    @Bean
    public DateFormat dateFormat() {
        return df;
    }
}
