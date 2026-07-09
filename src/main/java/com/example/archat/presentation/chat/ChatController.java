package com.example.archat.presentation.chat;

import com.example.archat.application.chat.AIChatUseCase;
import com.example.archat.application.chat.ChatUseCase;
import com.example.archat.domain.auth.AuthUser;
import com.example.archat.domain.chat.Chat;
import com.example.archat.presentation.common.BaseController;
import com.example.archat.infrastructure.session.SessionManager;
import com.example.archat.presentation.chat.ChatResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@WebServlet("/chat")
public class ChatController extends BaseController {
    private final ChatUseCase chatUseCase = AIChatUseCase.getInstance();
    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AuthUser authUser = sessionManager.getAuthUser(req);
        if (authUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        var rooms = chatUseCase.findAllRooms(authUser.userId())
                .stream()
                .map(ChatRoomResponseDTO::of)
                .toList();
        req.setAttribute("rooms", rooms);

        String roomId = req.getParameter("roomId");
        if (roomId == null && !rooms.isEmpty()) {
            roomId = rooms.get(0).getId();
        }

        req.setAttribute("currentRoomId", roomId);

        if (roomId != null) {
            List<ChatResponseDTO> response = chatUseCase.findAllByRoomId(roomId)
                    .stream()
                    .map(ChatResponseDTO::of)
                    .toList();
            req.setAttribute("chats", response);
        }

        req.setAttribute("currentUserEmail", authUser.email());

        req.getRequestDispatcher("%s/%s".formatted(VIEW_PREFIX, "chat.jsp"))
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AuthUser authUser = sessionManager.getAuthUser(req);
        if (authUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("createRoom".equals(action)) {
            String title = req.getParameter("title");
            if (title == null || title.trim().isEmpty()) {
                title = "새 대화방";
            }
            var room = chatUseCase.createRoom(authUser.userId(), title);
            resp.sendRedirect("%s/%s?roomId=%s".formatted(req.getContextPath(), "chat", room.id()));
            return;
        } else if ("deleteRoom".equals(action)) {
            String roomId = req.getParameter("roomId");
            if (roomId != null && !roomId.isEmpty()) {
                chatUseCase.deleteRoom(roomId);
            }
            resp.sendRedirect("%s/%s".formatted(req.getContextPath(), "chat"));
            return;
        } else if ("renameRoom".equals(action)) {
            String roomId = req.getParameter("roomId");
            String newTitle = req.getParameter("newTitle");
            if (roomId != null && newTitle != null && !newTitle.trim().isEmpty()) {
                chatUseCase.renameRoom(roomId, newTitle.trim());
            }
            resp.sendRedirect("%s/%s?roomId=%s".formatted(req.getContextPath(), "chat", roomId));
            return;
        }

        String roomId = req.getParameter("roomId");
        if (roomId == null || roomId.trim().isEmpty()) {
            var room = chatUseCase.createRoom(authUser.userId(), "새 대화방");
            roomId = room.id();
        }

        Chat chat = new Chat(
                req.getParameter("message"),
                "USER",
                authUser.userId(),
                roomId,
                req.getParameter("model"),
                ZonedDateTime.now().toString()
        );
        chatUseCase.save(chat);
        resp.sendRedirect("%s/%s?roomId=%s".formatted(req.getContextPath(), "chat", roomId));
    }
}
