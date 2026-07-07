package com.example.archat.infrastructure.session;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SessionCookieConfigListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext servletContext = sce.getServletContext();
        SessionCookieConfig cookieConfig = servletContext.getSessionCookieConfig();

        cookieConfig.setHttpOnly(true);
        cookieConfig.setSecure(Boolean.parseBoolean(System.getenv().getOrDefault("APP_COOKIE_SECURE", "false")));

        String cookiePath = servletContext.getContextPath();
        cookieConfig.setPath(cookiePath == null || cookiePath.isBlank() ? "/" : cookiePath);

        int sessionTimeoutMinutes = Integer.parseInt(System.getenv().getOrDefault("SESSION_TIMEOUT_MINUTES", "30"));
        servletContext.setSessionTimeout(sessionTimeoutMinutes);
    }
}
