package com.example.archat.presentation.chat;

import com.example.archat.application.chat.ChatUseCase;
import com.example.archat.domain.auth.AuthUser;
import com.example.archat.domain.chat.Chat;
import com.example.archat.infrastructure.session.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.FOUND;

@Controller
public class ChatController {

    private final ChatUseCase chatUseCase;
    private final SessionManager sessionManager;

    public ChatController(ChatUseCase chatUseCase, SessionManager sessionManager) {
        this.chatUseCase = chatUseCase;
        this.sessionManager = sessionManager;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam(required = false) String roomId,
            HttpServletRequest request,
            Model model
    ) {
        AuthUser authUser = sessionManager.getAuthUser(request);
        List<ChatRoomResponseDTO> rooms = chatUseCase.findAllRooms(authUser.userId())
                .stream()
                .map(ChatRoomResponseDTO::of)
                .toList();

        if (roomId == null && !rooms.isEmpty()) {
            roomId = rooms.get(0).id();
        }
        String selectedRoomId = roomId;
        if (selectedRoomId != null && rooms.stream().noneMatch(room -> room.id().equals(selectedRoomId))) {
            throw new ResponseStatusException(NOT_FOUND, "대화방을 찾을 수 없습니다.");
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("currentRoomId", selectedRoomId);
        model.addAttribute("currentUserEmail", authUser.email());

        if (selectedRoomId != null) {
            List<ChatResponseDTO> chats = chatUseCase.findAllByRoomId(authUser.userId(), selectedRoomId)
                    .stream()
                    .map(ChatResponseDTO::of)
                    .toList();
            model.addAttribute("chats", chats);
        }
        return "chat";
    }

    @PostMapping(value = "/chat", params = "action=createRoom")
    public String createRoom(
            @RequestParam(required = false) String title,
            HttpServletRequest request
    ) {
        AuthUser authUser = sessionManager.getAuthUser(request);
        String roomTitle = title == null || title.isBlank() ? "새 대화방" : title.trim();
        var room = chatUseCase.createRoom(authUser.userId(), roomTitle);
        return "redirect:/chat?roomId=" + room.id();
    }

    @PostMapping(value = "/chat", params = "action=deleteRoom")
    public String deleteRoom(@RequestParam String roomId, HttpServletRequest request) {
        AuthUser authUser = sessionManager.getAuthUser(request);
        chatUseCase.deleteRoom(authUser.userId(), roomId);
        return "redirect:/chat";
    }

    @PostMapping(value = "/chat", params = "action=renameRoom")
    public String renameRoom(
            @RequestParam String roomId,
            @RequestParam String newTitle,
            HttpServletRequest request
    ) {
        AuthUser authUser = sessionManager.getAuthUser(request);
        if (!newTitle.isBlank()) {
            chatUseCase.renameRoom(authUser.userId(), roomId, newTitle.trim());
        }
        return "redirect:/chat?roomId=" + roomId;
    }

    @PostMapping(value = "/chat", params = "!action")
    public ResponseEntity<?> sendMessage(
            @RequestParam(required = false) String roomId,
            @RequestParam String message,
            @RequestParam String model,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            @RequestHeader(value = "Accept", required = false) String accept,
            HttpServletRequest request
    ) {
        AuthUser authUser = sessionManager.getAuthUser(request);
        boolean newRoom = roomId == null || roomId.isBlank();
        if (newRoom) {
            roomId = chatUseCase.createRoom(authUser.userId(), "새 대화방").id();
        }

        Chat userChat = new Chat(
                message,
                "USER",
                authUser.userId(),
                roomId,
                model,
                ZonedDateTime.now().toString()
        );
        Chat aiChat = chatUseCase.save(userChat);

        boolean ajax = "XMLHttpRequest".equals(requestedWith)
                || (accept != null && accept.contains("application/json"));
        if (!newRoom && ajax) {
            return ResponseEntity.ok(ChatResponseDTO.of(aiChat));
        }
        return ResponseEntity.status(FOUND)
                .location(URI.create(request.getContextPath() + "/chat?roomId=" + roomId))
                .build();
    }
}
