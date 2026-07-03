package com.example.archat.presentation.controller;

import com.example.archat.application.service.GeminiChatService;
import com.example.archat.domain.model.Chat;
import com.example.archat.domain.service.ChatService;
import com.example.archat.presentation.dto.ChatResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@WebServlet("/chat")
public class ChatController extends BaseController {
    //    private GeminiChatService chatService;
    private ChatService chatService;
    // init

    @Override
    public void init() throws ServletException {
        chatService = GeminiChatService.getInstance(); // Lazy Loading
        // Service, Repository : static 저장해서 관리 <- tomcat이 자원 관리 X
        // Controller(Servlet) : tomcat 관리 - @WebServlet("/chat")
    }

    // get
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 접속 -> /chat
        // 데이터 불러오기
        HttpSession session = req.getSession(); // 세션 생성/불러오기 -> 유저를 구분
//        List<ChatResponseDTO> response = chatService.readHistory(session.getId())
        List<ChatResponseDTO> response = chatService.findAllByUserId(session.getId())
                // Stream -> map -> of(변환) -> jsp에서 최종적으로 만나게 되는...
                .stream()
                .map(ChatResponseDTO::of)
                .toList();

        // 세션 자체가 가지고 있는 id를 사용해서 인메모리 DB에서의 데이터를 구분
        req.setAttribute("chats",
                response);

        // 주소를 유지한채 jsp 포워딩 + 보안 + 가상 경로
        // webapp/WEB-INF/views/chat.jsp
        req.getRequestDispatcher("%s/%s".formatted(VIEW_PREFIX, "chat.jsp"))
                .forward(req, resp);
    }

    // post

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Chat chat = new Chat(
                req.getParameter("message"),
                "USER",
                req.getSession().getId(),
                req.getParameter("model"),
                ZonedDateTime.now().toString()
        );
//        chatService.sendMessage(chat);
        chatService.save(chat);
        resp.sendRedirect("%s/%s".formatted(req.getContextPath(), "chat"));
    }
}