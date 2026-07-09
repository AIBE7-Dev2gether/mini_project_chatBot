package com.example.archat.infrastructure.chat;

import com.example.archat.domain.chat.Chat;
import com.example.archat.domain.chat.ChatRepository;
import com.example.archat.infrastructure.db.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupabaseChatRepository implements ChatRepository {

    private SupabaseChatRepository() {}

    private static final SupabaseChatRepository instance = new SupabaseChatRepository();

    public static SupabaseChatRepository getInstance() {
        return instance;
    }

    @Override
    public void save(Chat chat) {
        String sql = "INSERT INTO chats (message, owner, user_id, model, timestamp) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, chat.message());
            pstmt.setString(2, chat.owner());
            pstmt.setString(3, chat.userId());
            pstmt.setString(4, chat.model());
            pstmt.setString(5, chat.timestamp());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB 저장 중 에러 발생", e);
        }
    }

    @Override
    public List<Chat> findAllByUserId(String userId) {
        String sql = "SELECT message, owner, user_id, model, timestamp FROM chats WHERE user_id = ? ORDER BY id ASC";
        List<Chat> chatList = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Chat chat = new Chat(
                            rs.getString("message"),
                            rs.getString("owner"),
                            rs.getString("user_id"),
                            rs.getString("model"),
                            rs.getString("timestamp")
                    );
                    chatList.add(chat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB 조회 중 에러 발생", e);
        }
        
        return chatList;
    }
}
