package com.example.archat.application.service;

import com.example.archat.domain.model.AuthUser;

public interface AuthService {
    AuthUser login(String email, String password);

    AuthUser signup(String email, String password);
}
