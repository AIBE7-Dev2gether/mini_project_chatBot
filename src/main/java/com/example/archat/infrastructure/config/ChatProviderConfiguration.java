package com.example.archat.infrastructure.config;

import com.example.archat.application.chat.ChatProvider;
import com.example.archat.infrastructure.chat.OpenAICompatibleProvider;
import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatProviderConfiguration {

    private static final String SYSTEM_INSTRUCTION = "친절한 말투로, 100자 이내로, 가능한 한글로 답변.";

    @Bean("groqChatProvider")
    ChatProvider groqChatProvider(AiProperties properties, ObjectMapper objectMapper) {
        return new OpenAICompatibleProvider(
                "https://api.groq.com/openai/v1/chat/completions",
                properties.groqApiKey(),
                SYSTEM_INSTRUCTION,
                512,
                objectMapper
        );
    }

    @Bean("nimChatProvider")
    ChatProvider nimChatProvider(AiProperties properties, ObjectMapper objectMapper) {
        return new OpenAICompatibleProvider(
                "https://integrate.api.nvidia.com/v1/chat/completions",
                properties.nimApiKey(),
                SYSTEM_INSTRUCTION,
                512,
                objectMapper
        );
    }
}
