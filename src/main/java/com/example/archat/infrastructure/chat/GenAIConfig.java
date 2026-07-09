package com.example.archat.infrastructure.chat;

import com.google.genai.Client;
import com.google.genai.types.*;

public class GenAIConfig {
    private static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");

    public static Client getClient() {
        return Client.builder()
                .apiKey(GEMINI_API_KEY).build();
    }

    private static final String SYSTEM_INSTRUCTION = "친절한 말투로, 100자 이내로, 가능한 한글로 답변.";

    public static GenerateContentConfig getGenerateContentConfig(String modelName) {
        GenerateContentConfig.Builder builder = GenerateContentConfig
                .builder()
                .maxOutputTokens(4096);

        // gemma-4-26b 모델(MoE)은 systemInstruction 파라미터를 받을 때 500 에러를 뱉는 알려진 이슈가 있어 분기 처리
        if (modelName == null || !modelName.contains("gemma-4-26b")) {
            builder.systemInstruction(
                    Content.builder()
                            .role("system")
                            .parts(Part.builder().text(SYSTEM_INSTRUCTION).build())
                            .build()
            );
        }

        if (modelName != null && modelName.contains("thinking")) {
            builder.thinkingConfig(
                    ThinkingConfig.builder()
                            .includeThoughts(false)
                            .thinkingLevel(ThinkingLevel.Known.MINIMAL)
                            .build()
            );
        }

        return builder.build();
    }

}