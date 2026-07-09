package com.example.archat.application.auth;

import com.example.archat.domain.auth.AuthUser;
import com.example.archat.domain.auth.AccountRepository;
import com.example.archat.infrastructure.auth.SupabaseAuthClient;
import com.example.archat.infrastructure.auth.SupabaseAccountRepository;

public class DefaultAuthUseCase implements AuthUseCase {

    private final AuthProvider authProvider;
    private final AccountRepository accountRepository;

    private DefaultAuthUseCase() {
        this.authProvider = SupabaseAuthClient.getInstance();
        this.accountRepository = SupabaseAccountRepository.getInstance();
    }

    private static final DefaultAuthUseCase instance = new DefaultAuthUseCase();

    public static DefaultAuthUseCase getInstance() {
        return instance;
    }

    @Override
    public AuthUser login(String email, String password) {
        AuthUser authUser = authProvider.login(email, password);
        accountRepository.upsert(authUser);
        return authUser;
    }

    @Override
    public AuthUser signup(String email, String password) {
        AuthUser authUser = authProvider.signup(email, password);
        accountRepository.upsert(authUser);
        return authUser;
    }
}
