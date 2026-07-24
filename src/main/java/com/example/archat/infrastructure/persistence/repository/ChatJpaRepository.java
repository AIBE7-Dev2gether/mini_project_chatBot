package com.example.archat.infrastructure.persistence.repository;

import com.example.archat.infrastructure.persistence.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {

    List<ChatEntity> findAllByChatRoomIdOrderByIdAsc(String roomId);

    long deleteAllByChatRoomId(String roomId);
}
