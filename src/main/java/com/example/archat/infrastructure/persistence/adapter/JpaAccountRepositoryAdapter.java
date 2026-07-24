package com.example.archat.infrastructure.persistence.adapter;

import com.example.archat.domain.auth.AccountRepository;
import com.example.archat.domain.auth.AuthUser;
import com.example.archat.infrastructure.persistence.entity.AccountEntity;
import com.example.archat.infrastructure.persistence.repository.AccountJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaAccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    public JpaAccountRepositoryAdapter(AccountJpaRepository accountJpaRepository) {
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    @Transactional
    public void upsert(AuthUser authUser) {
        AccountEntity account = accountJpaRepository.findById(authUser.userId())
                .orElseGet(() -> new AccountEntity(authUser.userId(), authUser.email()));
        account.updateEmail(authUser.email());
        accountJpaRepository.save(account);
    }
}
