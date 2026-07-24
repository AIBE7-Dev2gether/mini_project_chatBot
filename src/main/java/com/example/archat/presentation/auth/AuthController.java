package com.example.archat.presentation.auth;

import com.example.archat.application.auth.AuthException;
import com.example.archat.application.auth.AuthUseCase;
import com.example.archat.domain.auth.AuthUser;
import com.example.archat.infrastructure.session.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    private final SessionManager sessionManager;
    private final AuthUseCase authUseCase;

    public AuthController(SessionManager sessionManager, AuthUseCase authUseCase) {
        this.sessionManager = sessionManager;
        this.authUseCase = authUseCase;
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request) {
        return sessionManager.isAuthenticated(request) ? "redirect:/chat" : "login";
    }

    @GetMapping("/signup")
    public String signupPage(HttpServletRequest request) {
        return sessionManager.isAuthenticated(request) ? "redirect:/chat" : "signup";
    }

    @PostMapping("/login")
    public String loginPage(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request
    ) {
        try {
            AuthUser authUser = authUseCase.login(email, password);
            sessionManager.login(request, authUser);
            return "redirect:/chat";
        } catch (AuthException exception) {
            return "redirect:/login?error=" + encode(exception.userMessage());
        }
    }

    @PostMapping("/signup")
    public String signupPage(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request
    ) {
        try {
            AuthUser authUser = authUseCase.signup(email, password);
            sessionManager.login(request, authUser);
            return "redirect:/chat";
        } catch (AuthException exception) {
            return "redirect:/signup?error=" + encode(exception.userMessage());
        }
    }

    @PostMapping("/logout")
    public String logoutPage(HttpServletRequest request) {
        sessionManager.logout(request);
        return "redirect:/login?logout=1";
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginApi(
            @RequestBody LoginPayload payload,
            HttpServletRequest request
    ) {
        try {
            AuthUser authUser = authUseCase.login(payload.email(), payload.password());
            sessionManager.login(request, authUser);
            return ResponseEntity.ok(buildAuthResponse(true, authUser, null, null));
        } catch (AuthException exception) {
            return ResponseEntity.status(exception.statusCode())
                    .body(buildAuthResponse(false, null, exception.code(), exception.userMessage()));
        }
    }

    @PostMapping("/api/auth/signup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> signupApi(
            @RequestBody LoginPayload payload,
            HttpServletRequest request
    ) {
        try {
            AuthUser authUser = authUseCase.signup(payload.email(), payload.password());
            sessionManager.login(request, authUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(buildAuthResponse(true, authUser, null, null));
        } catch (AuthException exception) {
            return ResponseEntity.status(exception.statusCode())
                    .body(buildAuthResponse(false, null, exception.code(), exception.userMessage()));
        }
    }

    @PostMapping("/api/auth/logout")
    @ResponseBody
    public Map<String, Object> logoutApi(HttpServletRequest request) {
        sessionManager.logout(request);
        return Map.of("authenticated", false);
    }

    @GetMapping("/api/auth/me")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        AuthUser authUser = sessionManager.getAuthUser(request);
        if (authUser != null) {
            return ResponseEntity.ok(buildAuthResponse(true, authUser, null, null));
        }

        String code = request.getRequestedSessionId() != null && !request.isRequestedSessionIdValid()
                ? "session_expired"
                : "unauthenticated";
        String message = "session_expired".equals(code)
                ? "세션이 만료되었습니다."
                : "로그인이 필요합니다.";
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildAuthResponse(false, null, code, message));
    }

    private Map<String, Object> buildAuthResponse(
            boolean authenticated,
            AuthUser authUser,
            String errorCode,
            String errorMessage
    ) {
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authenticated);
        if (authUser != null) {
            response.put("user", Map.of("id", authUser.userId(), "email", authUser.email()));
        }
        if (errorCode != null) {
            response.put("error", errorCode);
        }
        if (errorMessage != null) {
            response.put("message", errorMessage);
        }
        return response;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public record LoginPayload(String email, String password) {
    }
}
