package com.example.archat.presentation.controller;

import com.example.archat.application.service.AuthException;
import com.example.archat.application.service.AuthService;
import com.example.archat.application.service.SupabaseAuthService;
import com.example.archat.domain.model.AuthUser;
import com.example.archat.infrastructure.session.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/login", "/signup", "/logout", "/api/auth/*"})
public class AuthController extends BaseController {

    private final SessionManager sessionManager = new SessionManager();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = SupabaseAuthService.getInstance();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/login".equals(servletPath)) {
            if (sessionManager.isAuthenticated(req)) {
                resp.sendRedirect(req.getContextPath() + "/chat");
                return;
            }

            req.getRequestDispatcher("%s/%s".formatted(VIEW_PREFIX, "login.jsp"))
                    .forward(req, resp);
            return;
        }

        if ("/signup".equals(servletPath)) {
            if (sessionManager.isAuthenticated(req)) {
                resp.sendRedirect(req.getContextPath() + "/chat");
                return;
            }

            req.getRequestDispatcher("%s/%s".formatted(VIEW_PREFIX, "signup.jsp"))
                    .forward(req, resp);
            return;
        }

        if ("/api/auth".equals(servletPath) && "/me".equals(req.getPathInfo())) {
            handleMe(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/login".equals(servletPath)) {
            handleLoginPage(req, resp);
            return;
        }

        if ("/signup".equals(servletPath)) {
            handleSignupPage(req, resp);
            return;
        }

        if ("/logout".equals(servletPath)) {
            sessionManager.logout(req);
            resp.sendRedirect(req.getContextPath() + "/login?logout=1");
            return;
        }

        String pathInfo = req.getPathInfo();
        if ("/login".equals(pathInfo)) {
            handleLoginApi(req, resp);
            return;
        }
        if ("/signup".equals(pathInfo)) {
            handleSignupApi(req, resp);
            return;
        }
        if ("/logout".equals(pathInfo)) {
            handleLogoutApi(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleLoginPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginPayload loginPayload = extractLoginPayload(req);

        try {
            AuthUser authUser = authService.login(loginPayload.email(), loginPayload.password());
            sessionManager.login(req, authUser);
            resp.sendRedirect(req.getContextPath() + "/chat");
        } catch (AuthException e) {
            resp.sendRedirect(req.getContextPath() + "/login?error=" + encode(e.userMessage()));
        }
    }

    private void handleLoginApi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginPayload loginPayload = extractLoginPayload(req);

        resp.setContentType("application/json;charset=UTF-8");

        try {
            AuthUser authUser = authService.login(loginPayload.email(), loginPayload.password());
            sessionManager.login(req, authUser);
            writeJson(resp, HttpServletResponse.SC_OK, buildAuthResponse(true, authUser, null, null));
        } catch (AuthException e) {
            writeJson(resp, e.statusCode(), buildAuthResponse(false, null, e.code(), e.userMessage()));
        }
    }

    private void handleSignupPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginPayload signupPayload = extractLoginPayload(req);

        try {
            AuthUser authUser = authService.signup(signupPayload.email(), signupPayload.password());
            sessionManager.login(req, authUser);
            resp.sendRedirect(req.getContextPath() + "/chat");
        } catch (AuthException e) {
            resp.sendRedirect(req.getContextPath() + "/signup?error=" + encode(e.userMessage()));
        }
    }

    private void handleSignupApi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginPayload signupPayload = extractLoginPayload(req);

        try {
            AuthUser authUser = authService.signup(signupPayload.email(), signupPayload.password());
            sessionManager.login(req, authUser);
            writeJson(resp, HttpServletResponse.SC_CREATED, buildAuthResponse(true, authUser, null, null));
        } catch (AuthException e) {
            writeJson(resp, e.statusCode(), buildAuthResponse(false, null, e.code(), e.userMessage()));
        }
    }

    private void handleLogoutApi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sessionManager.logout(req);
        writeJson(resp, HttpServletResponse.SC_OK, Map.of("authenticated", false));
    }

    private void handleMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        AuthUser authUser = sessionManager.getAuthUser(req);
        if (authUser == null) {
            String code = req.getRequestedSessionId() != null && !req.isRequestedSessionIdValid()
                    ? "session_expired"
                    : "unauthenticated";
            String message = "session_expired".equals(code)
                    ? "세션이 만료되었습니다."
                    : "로그인이 필요합니다.";
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, buildAuthResponse(false, null, code, message));
            return;
        }

        writeJson(resp, HttpServletResponse.SC_OK, buildAuthResponse(true, authUser, null, null));
    }

    private Map<String, Object> buildAuthResponse(boolean authenticated, AuthUser authUser, String errorCode, String errorMessage) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("authenticated", authenticated);

        if (authUser != null) {
            payload.put("user", Map.of(
                    "id", authUser.userId(),
                    "email", authUser.email()
            ));
        }

        if (errorCode != null) {
            payload.put("error", errorCode);
        }

        if (errorMessage != null) {
            payload.put("message", errorMessage);
        }

        return payload;
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(resp.getWriter(), body);
    }

    private LoginPayload extractLoginPayload(HttpServletRequest req) throws IOException {
        String contentType = req.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            return objectMapper.readValue(req.getInputStream(), LoginPayload.class);
        }

        return new LoginPayload(req.getParameter("email"), req.getParameter("password"));
    }

    private record LoginPayload(String email, String password) {
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
