package com.example.archat.infrastructure.persistence.adapter;

import com.example.archat.domain.chat.ChatRoom;
import com.example.archat.domain.chat.ChatRoomRepository;
import com.example.archat.infrastructure.persistence.entity.AccountEntity;
import com.example.archat.infrastructure.persistence.entity.ChatRoomEntity;
import com.example.archat.infrastructure.persistence.repository.AccountJpaRepository;
import com.example.archat.infrastructure.persistence.repository.ChatRoomJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;

@Repository
public class JpaChatRoomRepositoryAdapter implements ChatRoomRepository {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    public JpaChatRoomRepositoryAdapter(
            ChatRoomJpaRepository chatRoomJpaRepository,
            AccountJpaRepository accountJpaRepository
    ) {
        this.chatRoomJpaRepository = chatRoomJpaRepository;
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    @Transactional
    public void save(ChatRoom chatRoom) {
        AccountEntity account = accountJpaRepository.getReferenceById(chatRoom.userId());
        chatRoomJpaRepository.save(new ChatRoomEntity(chatRoom.id(), account, chatRoom.title()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoom> findAllByUserId(String userId) {
        return chatRoomJpaRepository.findAllByAccountUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndUserId(String id, String userId) {
        return chatRoomJpaRepository.existsByIdAndAccountUserId(id, userId);
    }

    @Override
    @Transactional
    public void deleteByIdAndUserId(String id, String userId) {
        chatRoomJpaRepository.deleteByIdAndAccountUserId(id, userId);
    }

    @Override
    @Transactional
    public void updateTitle(String id, String userId, String title) {
        ChatRoomEntity room = chatRoomJpaRepository.findByIdAndAccountUserId(id, userId)
                .orElseThrow(() -> new IllegalArgumentException("대화방을 찾을 수 없습니다."));
        room.rename(title);
    }

    private ChatRoom toDomain(ChatRoomEntity entity) {
        return new ChatRoom(
                entity.getId(),
                entity.getAccount().getUserId(),
                entity.getTitle(),
                entity.getCreatedAt().atZone(ZoneOffset.UTC).toString()
        );
    }
}
