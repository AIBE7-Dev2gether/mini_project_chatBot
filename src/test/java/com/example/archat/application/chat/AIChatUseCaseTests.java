package com.example.archat.application.chat;

import com.example.archat.domain.chat.ChatRepository;
import com.example.archat.domain.chat.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIChatUseCaseTests {

    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatProvider groqProvider;
    @Mock
    private ChatProvider geminiProvider;
    @Mock
    private ChatProvider nimProvider;

    private AIChatUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new AIChatUseCase(
                chatRepository,
                chatRoomRepository,
                groqProvider,
                geminiProvider,
                nimProvider
        );
    }

    @Test
    void rejectsReadingAnotherUsersRoom() {
        when(chatRoomRepository.existsByIdAndUserId("room", "other-user")).thenReturn(false);

        assertThatThrownBy(() -> useCase.findAllByRoomId("other-user", "room"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("접근할 수 없는 대화방입니다.");

        verify(chatRepository, never()).findAllByRoomId("room");
    }

    @Test
    void deletesMessagesAndRoomOnlyAfterOwnershipCheck() {
        when(chatRoomRepository.existsByIdAndUserId("room", "owner")).thenReturn(true);

        useCase.deleteRoom("owner", "room");

        verify(chatRepository).deleteByRoomId("room");
        verify(chatRoomRepository).deleteByIdAndUserId("room", "owner");
    }
}
