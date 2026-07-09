package com.example.archat.domain.auth;

public interface AccountRepository {
    void upsert(AuthUser authUser);
}
