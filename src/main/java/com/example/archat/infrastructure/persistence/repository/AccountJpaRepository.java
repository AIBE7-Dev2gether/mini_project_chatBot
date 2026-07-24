package com.example.archat.infrastructure.persistence.repository;

import com.example.archat.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {
}
