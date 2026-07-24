package com.example.archat.infrastructure.persistence;

import com.example.archat.domain.auth.AccountRepository;
import com.example.archat.domain.auth.AuthUser;
import com.example.archat.domain.chat.Chat;
import com.example.archat.domain.chat.ChatRepository;
import com.example.archat.domain.chat.ChatRoom;
import com.example.archat.domain.chat.ChatRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "AIVEN_MYSQL_HOST", matches = ".+")
class AivenMySqlSmokeTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Test
    void persistsAndReadsTheCompleteAggregateOnAivenMySql() {
        String suffix = UUID.randomUUID().toString();
        String userId = "smoke-user-" + suffix;
        String roomId = "smoke-room-" + suffix;
        String timestamp = ZonedDateTime.now().toString();

        accountRepository.upsert(new AuthUser(userId, suffix + "@smoke.invalid"));
        chatRoomRepository.save(new ChatRoom(roomId, userId, "Aiven smoke test", timestamp));
        chatRepository.save(new Chat(
                "Aiven MySQL 연결 검증 메시지 😀",
                "USER",
                userId,
                roomId,
                "smoke-model",
                timestamp
        ));

        assertThat(chatRoomRepository.findAllByUserId(userId))
                .extracting(ChatRoom::id)
                .containsExactly(roomId);
        assertThat(chatRepository.findAllByRoomId(roomId))
                .singleElement()
                .satisfies(chat -> {
                    assertThat(chat.message()).isEqualTo("Aiven MySQL 연결 검증 메시지 😀");
                    assertThat(chat.owner()).isEqualTo("USER");
                    assertThat(chat.roomId()).isEqualTo(roomId);
                });
    }
}
