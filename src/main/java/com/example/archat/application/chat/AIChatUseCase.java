package com.example.archat.application.chat;

import com.example.archat.domain.chat.Chat;
import com.example.archat.domain.chat.ChatRepository;
import com.example.archat.domain.chat.ChatRoom;
import com.example.archat.domain.chat.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AIChatUseCase implements ChatUseCase {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatProvider groqChatProvider;
    private final ChatProvider genAIChatProvider;
    private final ChatProvider nimChatProvider;

    public AIChatUseCase(
            ChatRepository chatRepository,
            ChatRoomRepository chatRoomRepository,
            @Qualifier("groqChatProvider") ChatProvider groqChatProvider,
            @Qualifier("genAIChatProvider") ChatProvider genAIChatProvider,
            @Qualifier("nimChatProvider") ChatProvider nimChatProvider
    ) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.groqChatProvider = groqChatProvider;
        this.genAIChatProvider = genAIChatProvider;
        this.nimChatProvider = nimChatProvider;
    }

    @Override
    public Chat save(Chat chat) {
        requireOwnedRoom(chat.roomId(), chat.userId());
        chatRepository.save(chat);
        List<Chat> history = chatRepository.findAllByRoomId(chat.roomId());

        String aiResponse = selectProvider(chat.model()).useAI(chat, history);
        Chat aiChat = new Chat(
                aiResponse,
                "AI",
                chat.userId(),
                chat.roomId(),
                chat.model(),
                ZonedDateTime.now().toString()
        );
        chatRepository.save(aiChat);
        return aiChat;
    }

    @Override
    public List<Chat> findAllByRoomId(String userId, String roomId) {
        requireOwnedRoom(roomId, userId);
        return chatRepository.findAllByRoomId(roomId);
    }

    @Override
    public ChatRoom createRoom(String userId, String title) {
        ChatRoom room = new ChatRoom(
                UUID.randomUUID().toString(),
                userId,
                title,
                ZonedDateTime.now().toString()
        );
        chatRoomRepository.save(room);
        return room;
    }

    @Override
    public List<ChatRoom> findAllRooms(String userId) {
        return chatRoomRepository.findAllByUserId(userId);
    }

    @Override
    @Transactional
    public void deleteRoom(String userId, String roomId) {
        requireOwnedRoom(roomId, userId);
        chatRepository.deleteByRoomId(roomId);
        chatRoomRepository.deleteByIdAndUserId(roomId, userId);
    }

    @Override
    @Transactional
    public void renameRoom(String userId, String roomId, String title) {
        chatRoomRepository.updateTitle(roomId, userId, title);
    }

    private ChatProvider selectProvider(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("AI 모델을 선택해 주세요.");
        }
        if (model.contains("gemini") || model.contains("gemma")) {
            return genAIChatProvider;
        }
        if (model.contains("nvidia") || model.contains("nemotron") || model.startsWith("meta/")) {
            return nimChatProvider;
        }
        return groqChatProvider;
    }

    private void requireOwnedRoom(String roomId, String userId) {
        if (!chatRoomRepository.existsByIdAndUserId(roomId, userId)) {
            throw new IllegalArgumentException("접근할 수 없는 대화방입니다.");
        }
    }
}
