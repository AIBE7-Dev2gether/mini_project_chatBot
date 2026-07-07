package com.example.archat.application.service;

import com.example.archat.domain.model.AuthUser;
import com.example.archat.domain.repository.AccountRepository;
import com.example.archat.infrastructure.api.SupabaseAuthClient;
import com.example.archat.infrastructure.repository.SupabaseAccountRepository;

public class SupabaseAuthService implements AuthService {

    private final SupabaseAuthClient supabaseAuthClient;
    private final AccountRepository accountRepository;

    private SupabaseAuthService() {
        this.supabaseAuthClient = SupabaseAuthClient.getInstance();
        this.accountRepository = SupabaseAccountRepository.getInstance();
    }

    private static final SupabaseAuthService instance = new SupabaseAuthService();

    public static SupabaseAuthService getInstance() {
        return instance;
    }

    @Override
    public AuthUser login(String email, String password) {
        AuthUser authUser = supabaseAuthClient.login(email, password);
        accountRepository.upsert(authUser);
        return authUser;
    }

    @Override
    public AuthUser signup(String email, String password) {
        AuthUser authUser = supabaseAuthClient.signup(email, password);
        accountRepository.upsert(authUser);
        return authUser;
    }
}
