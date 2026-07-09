package com.example.archat.infrastructure.chat;

public class NimConfig {
    public static final String NIM_API_KEY = System.getenv("NIM_API_KEY");
    public static final String ENDPOINT = "https://integrate.api.nvidia.com/v1/chat/completions";
    public static final String SYSTEM_INSTRUCTION = "친절한 말투로, 100자 이내로, 가능한 한글로 답변.";
    public static final int MAX_TOKENS = 512;

    private NimConfig() {}
}