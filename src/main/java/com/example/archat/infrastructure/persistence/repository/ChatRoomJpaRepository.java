package com.example.archat.infrastructure.persistence.repository;

import com.example.archat.infrastructure.persistence.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomJpaRepository extends JpaRepository<ChatRoomEntity, String> {

    List<ChatRoomEntity> findAllByAccountUserIdOrderByCreatedAtDesc(String userId);

    boolean existsByIdAndAccountUserId(String id, String userId);

    Optional<ChatRoomEntity> findByIdAndAccountUserId(String id, String userId);

    long deleteByIdAndAccountUserId(String id, String userId);
}
