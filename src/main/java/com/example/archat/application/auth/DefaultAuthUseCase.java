package com.example.archat.application.auth;

import com.example.archat.domain.auth.AuthUser;
import com.example.archat.domain.auth.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class DefaultAuthUseCase implements AuthUseCase {

    private final AuthProvider authProvider;
    private final AccountRepository accountRepository;

    public DefaultAuthUseCase(AuthProvider authProvider, AccountRepository accountRepository) {
        this.authProvider = authProvider;
        this.accountRepository = accountRepository;
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
