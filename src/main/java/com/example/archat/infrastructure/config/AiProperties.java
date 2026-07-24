package com.example.archat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String geminiApiKey,
        String groqApiKey,
        String nimApiKey
) {
}
