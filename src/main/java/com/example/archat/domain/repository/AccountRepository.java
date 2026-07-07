package com.example.archat.domain.repository;

import com.example.archat.domain.model.AuthUser;

public interface AccountRepository {
    void upsert(AuthUser authUser);
}
