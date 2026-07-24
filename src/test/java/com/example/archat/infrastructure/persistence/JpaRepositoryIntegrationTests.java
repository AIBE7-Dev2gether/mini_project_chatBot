package com.example.archat.infrastructure.persistence;

import com.example.archat.domain.auth.AccountRepository;
import com.example.archat.domain.auth.AuthUser;
import com.example.archat.domain.chat.Chat;
import com.example.archat.domain.chat.ChatRepository;
import com.example.archat.domain.chat.ChatRoom;
import com.example.archat.domain.chat.ChatRoomRepository;
import com.example.archat.infrastructure.persistence.adapter.JpaAccountRepositoryAdapter;
import com.example.archat.infrastructure.persistence.adapter.JpaChatRepositoryAdapter;
import com.example.archat.infrastructure.persistence.adapter.JpaChatRoomRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        JpaAccountRepositoryAdapter.class,
        JpaChatRoomRepositoryAdapter.class,
        JpaChatRepositoryAdapter.class
})
class JpaRepositoryIntegrationTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Test
    void savesAndReadsRoomsAndChatsInExpectedOrder() {
        String userId = "user-1";
        String roomId = "room-1";
        accountRepository.upsert(new AuthUser(userId, "user@example.com"));
        chatRoomRepository.save(new ChatRoom(roomId, userId, "첫 번째 방", ZonedDateTime.now().toString()));

        chatRepository.save(new Chat(
                "첫 메시지", "USER", userId, roomId, "gemini-test", ZonedDateTime.now().toString()
        ));
        chatRepository.save(new Chat(
                "두 번째 메시지", "AI", userId, roomId, "gemini-test", ZonedDateTime.now().toString()
        ));

        assertThat(chatRoomRepository.findAllByUserId(userId))
                .extracting(ChatRoom::id)
                .containsExactly(roomId);
        assertThat(chatRepository.findAllByRoomId(roomId))
                .extracting(Chat::message)
                .containsExactly("첫 메시지", "두 번째 메시지");
    }

    @Test
    void updatesAndDeletesOnlyRoomsOwnedByTheUser() {
        accountRepository.upsert(new AuthUser("owner", "owner@example.com"));
        accountRepository.upsert(new AuthUser("other", "other@example.com"));
        chatRoomRepository.save(new ChatRoom("room", "owner", "이전 제목", ZonedDateTime.now().toString()));

        assertThat(chatRoomRepository.existsByIdAndUserId("room", "owner")).isTrue();
        assertThat(chatRoomRepository.existsByIdAndUserId("room", "other")).isFalse();

        chatRoomRepository.updateTitle("room", "owner", "새 제목");
        assertThat(chatRoomRepository.findAllByUserId("owner"))
                .extracting(ChatRoom::title)
                .containsExactly("새 제목");

        chatRoomRepository.deleteByIdAndUserId("room", "other");
        assertThat(chatRoomRepository.existsByIdAndUserId("room", "owner")).isTrue();

        chatRoomRepository.deleteByIdAndUserId("room", "owner");
        assertThat(chatRoomRepository.existsByIdAndUserId("room", "owner")).isFalse();
    }
}
