package com.example.archat.infrastructure.persistence.adapter;

import com.example.archat.domain.chat.Chat;
import com.example.archat.domain.chat.ChatRepository;
import com.example.archat.infrastructure.persistence.entity.ChatEntity;
import com.example.archat.infrastructure.persistence.entity.ChatRoomEntity;
import com.example.archat.infrastructure.persistence.repository.ChatJpaRepository;
import com.example.archat.infrastructure.persistence.repository.ChatRoomJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Repository
public class JpaChatRepositoryAdapter implements ChatRepository {

    private final ChatJpaRepository chatJpaRepository;
    private final ChatRoomJpaRepository chatRoomJpaRepository;

    public JpaChatRepositoryAdapter(
            ChatJpaRepository chatJpaRepository,
            ChatRoomJpaRepository chatRoomJpaRepository
    ) {
        this.chatJpaRepository = chatJpaRepository;
        this.chatRoomJpaRepository = chatRoomJpaRepository;
    }

    @Override
    @Transactional
    public void save(Chat chat) {
        ChatRoomEntity room = chatRoomJpaRepository.getReferenceById(chat.roomId());
        ChatEntity entity = new ChatEntity(
                chat.message(),
                chat.owner(),
                chat.userId(),
                room,
                chat.model(),
                parseTimestamp(chat.timestamp())
        );
        chatJpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> findAllByRoomId(String roomId) {
        return chatJpaRepository.findAllByChatRoomIdOrderByIdAsc(roomId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByRoomId(String roomId) {
        chatJpaRepository.deleteAllByChatRoomId(roomId);
    }

    private Chat toDomain(ChatEntity entity) {
        String timestamp = entity.getSentAt() == null
                ? null
                : entity.getSentAt().atZone(ZoneOffset.UTC).toString();
        return new Chat(
                entity.getMessage(),
                entity.getOwner(),
                entity.getUserId(),
                entity.getChatRoom().getId(),
                entity.getModel(),
                timestamp
        );
    }

    private LocalDateTime parseTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(timestamp)
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("지원하지 않는 채팅 시간 형식입니다: " + timestamp, exception);
        }
    }
}
