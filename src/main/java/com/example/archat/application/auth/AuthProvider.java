package com.example.archat.application.auth;

import com.example.archat.domain.auth.AuthUser;

public interface AuthProvider {
    AuthUser login(String email, String password);
    AuthUser signup(String email, String password);
}
