package com.example.archat.application.auth;

public class AuthException extends RuntimeException {

    private final int statusCode;
    private final String code;
    private final String userMessage;

    public AuthException(int statusCode, String code, String userMessage) {
        super(userMessage);
        this.statusCode = statusCode;
        this.code = code;
        this.userMessage = userMessage;
    }

    public int statusCode() {
        return statusCode;
    }

    public String code() {
        return code;
    }

    public String userMessage() {
        return userMessage;
    }
}
