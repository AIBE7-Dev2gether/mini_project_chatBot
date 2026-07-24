package com.example.archat.infrastructure.auth;

import com.example.archat.application.auth.AuthException;
import com.example.archat.application.auth.AuthProvider;
import com.example.archat.domain.auth.AuthUser;
import com.example.archat.infrastructure.config.SupabaseProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class SupabaseAuthClient implements AuthProvider {

    private final SupabaseProperties properties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SupabaseAuthClient(SupabaseProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public AuthUser login(String email, String password) {
        validateLoginEnvironment();

        try {
            String requestBody = objectMapper.writeValueAsString(new LoginRequest(email, password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("%s/auth/v1/token?grant_type=password".formatted(properties.url())))
                    .header("Content-Type", "application/json")
                    .header("apikey", properties.anonKey())
                    .header("Authorization", "Bearer " + properties.anonKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = send(request);
            ensureSuccess(response, "login_failed");

            SupabaseTokenResponse tokenResponse = objectMapper.readValue(response.body(), SupabaseTokenResponse.class);
            if (tokenResponse.user() == null || tokenResponse.user().id() == null || tokenResponse.user().email() == null) {
                throw new AuthException(500, "invalid_auth_response", "Supabase 응답에 사용자 정보가 없습니다.");
            }

            return new AuthUser(tokenResponse.user().id(), tokenResponse.user().email());
        } catch (IOException e) {
            throw new RuntimeException("Supabase 응답 파싱 중 오류가 발생했습니다.", e);
        }
    }

    public AuthUser signup(String email, String password) {
        validateSignupEnvironment();

        try {
            String requestBody = objectMapper.writeValueAsString(new SignupRequest(email, password, true));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("%s/auth/v1/admin/users".formatted(properties.url())))
                    .header("Content-Type", "application/json")
                    .header("apikey", properties.serviceRoleKey())
                    .header("Authorization", "Bearer " + properties.serviceRoleKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = send(request);
            ensureSuccess(response, "signup_failed");

            SupabaseUser user = objectMapper.readValue(response.body(), SupabaseUser.class);
            if (user.id() == null || user.email() == null) {
                throw new AuthException(500, "invalid_auth_response", "Supabase 응답에 사용자 정보가 없습니다.");
            }

            return new AuthUser(user.id(), user.email());
        } catch (IOException e) {
            throw new RuntimeException("Supabase 회원가입 응답 파싱 중 오류가 발생했습니다.", e);
        }
    }

    private void validateLoginEnvironment() {
        if (isBlank(properties.url()) || isBlank(properties.anonKey())) {
            throw new AuthException(500, "auth_config_missing", "SUPABASE_URL 또는 SUPABASE_ANON_KEY 환경 변수가 설정되지 않았습니다.");
        }
    }

    private void validateSignupEnvironment() {
        if (isBlank(properties.url()) || isBlank(properties.serviceRoleKey())) {
            throw new AuthException(500, "auth_config_missing", "SUPABASE_URL 또는 SUPABASE_SERVICE_ROLE_KEY 환경 변수가 설정되지 않았습니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Supabase 요청 처리 중 오류가 발생했습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Supabase 요청이 중단되었습니다.", e);
        }
    }

    private void ensureSuccess(HttpResponse<String> response, String defaultCode) throws IOException {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }

        SupabaseErrorResponse errorResponse = objectMapper.readValue(response.body(), SupabaseErrorResponse.class);
        throw mapError(response.statusCode(), defaultCode, errorResponse);
    }

    private AuthException mapError(int statusCode, String defaultCode, SupabaseErrorResponse errorResponse) {
        String description = firstNonBlank(errorResponse.msg(), errorResponse.errorDescription(), errorResponse.error(), "인증 처리에 실패했습니다.");

        if (statusCode == 400 || statusCode == 401) {
            if (containsAny(description, "invalid login credentials", "email not confirmed", "invalid_credentials")) {
                return new AuthException(401, "invalid_credentials", "이메일 또는 비밀번호가 올바르지 않습니다.");
            }
        }

        if (statusCode == 422 || statusCode == 429 || statusCode == 400) {
            if (containsAny(description, "already been registered", "already registered", "user already registered", "duplicate")) {
                return new AuthException(409, "email_already_exists", "이미 가입된 이메일입니다.");
            }
            if (containsAny(description, "password should", "password")) {
                return new AuthException(400, "invalid_password", "비밀번호 형식이 Supabase 정책을 만족하지 않습니다.");
            }
            if (containsAny(description, "email address", "email")) {
                return new AuthException(400, "invalid_email", "이메일 형식이 올바르지 않습니다.");
            }
        }

        return new AuthException(statusCode >= 400 ? statusCode : 500, defaultCode, description);
    }

    private boolean containsAny(String text, String... candidates) {
        String lower = text == null ? "" : text.toLowerCase();
        for (String candidate : candidates) {
            if (lower.contains(candidate.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private record LoginRequest(String email, String password) {
    }

    private record SignupRequest(
            String email,
            String password,
            @JsonProperty("email_confirm") boolean emailConfirm
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SupabaseTokenResponse(
            @JsonProperty("access_token") String accessToken,
            SupabaseUser user
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SupabaseUser(String id, String email) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SupabaseErrorResponse(
            String error,
            String msg,
            @JsonProperty("error_description") String errorDescription
    ) {
    }
}
