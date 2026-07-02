package com.example.archat.infrastructure.api;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;

public class GenAIConfig {
    private static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");

    public static Client getClient() {
        return Client.builder()
                .apiKey(GEMINI_API_KEY).build();
    }

    private static final String SYSTEM_INSTRUCTION = "친절한 말투로, 100자 이내로, 가능한 한글로 답변.";

    public static GenerateContentConfig getGenerateContentConfig() {
        return GenerateContentConfig
                .builder()
                .maxOutputTokens(512)
                .systemInstruction(
                        Content.builder().parts(
                                Part.builder().text(SYSTEM_INSTRUCTION).build()).build())
                .build();
    }

}