package com.stockscreener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class StockScreenerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockScreenerApplication.class, args);
    }
}