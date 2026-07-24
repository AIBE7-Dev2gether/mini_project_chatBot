package com.example.archat.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.supabase")
public record SupabaseProperties(
        String url,
        String anonKey,
        String serviceRoleKey
) {
}
