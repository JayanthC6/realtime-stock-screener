package com.stockscreener.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Real-Time Stock Screener API",
        version = "1.0.0",
        description = "REST API for live stock data, RSI-based screening, and price alerts. " +
                      "Built with Spring Boot, Redis Pub/Sub, and Finnhub WebSocket feed.",
        contact = @Contact(name = "Stock Screener Project")
    ),
    servers = @Server(url = "http://localhost:8081", description = "Local Dev Server")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Paste your JWT token from POST /api/auth/login"
)
public class OpenApiConfig {
    // Configuration is declarative via annotations above
}
