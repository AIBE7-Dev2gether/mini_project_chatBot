package com.example.archat.infrastructure.session;

import com.example.archat.domain.auth.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    public static final String AUTH_USER_ID = "authUserId";
    public static final String AUTH_USER_EMAIL = "authUserEmail";

    public void login(HttpServletRequest req, AuthUser authUser) {
        HttpSession existingSession = req.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute(AUTH_USER_ID, authUser.userId());
        session.setAttribute(AUTH_USER_EMAIL, authUser.email());
    }

    public void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public AuthUser getAuthUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute(AUTH_USER_ID);
        Object email = session.getAttribute(AUTH_USER_EMAIL);
        if (!(userId instanceof String) || !(email instanceof String)) {
            return null;
        }

        return new AuthUser((String) userId, (String) email);
    }

    public boolean isAuthenticated(HttpServletRequest req) {
        return getAuthUser(req) != null;
    }
}
