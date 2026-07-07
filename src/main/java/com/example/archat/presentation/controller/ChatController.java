package com.example.archat.presentation.controller;

import com.example.archat.application.service.AIChatService;
import com.example.archat.domain.model.AuthUser;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.service.ChatService;
import com.example.archat.infrastructure.session.SessionManager;
import com.example.archat.presentation.dto.ChatResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@WebServlet("/chat")
public class ChatController extends BaseController {
    private ChatService chatService;
    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void init() throws ServletException {
        chatService = AIChatService.getInstance(); // Lazy Loading
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AuthUser authUser = sessionManager.getAuthUser(req);
        if (authUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<ChatResponseDTO> response = chatService.findAllByUserId(authUser.userId())
                .stream()
                .map(ChatResponseDTO::of)
                .toList();

        req.setAttribute("chats", response);
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

        Chat chat = new Chat(
                req.getParameter("message"),
                "USER",
                authUser.userId(),
                req.getParameter("model"),
                ZonedDateTime.now().toString()
        );
        chatService.save(chat);
        resp.sendRedirect("%s/%s".formatted(req.getContextPath(), "chat"));
    }
}
