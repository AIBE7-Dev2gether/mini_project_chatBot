package com.example.archat.presentation.common;

import com.example.archat.infrastructure.session.SessionManager;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(urlPatterns = {"/chat"})
public class AuthFilter implements Filter {

    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (!sessionManager.isAuthenticated(req)) {
            boolean expiredSession = req.getRequestedSessionId() != null && !req.isRequestedSessionIdValid();
            String redirectUrl = req.getContextPath() + "/login" + (expiredSession ? "?expired=1" : "");
            resp.sendRedirect(redirectUrl);
            return;
        }

        chain.doFilter(request, response);
    }
}
