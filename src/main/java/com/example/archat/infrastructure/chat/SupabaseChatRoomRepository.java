package com.example.archat.infrastructure.chat;

import com.example.archat.domain.chat.ChatRoom;
import com.example.archat.domain.chat.ChatRoomRepository;
import com.example.archat.infrastructure.db.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupabaseChatRoomRepository implements ChatRoomRepository {

    private SupabaseChatRoomRepository() {}

    private static final SupabaseChatRoomRepository instance = new SupabaseChatRoomRepository();

    public static SupabaseChatRoomRepository getInstance() {
        return instance;
    }

    @Override
    public void save(ChatRoom chatRoom) {
        String sql = "INSERT INTO chat_rooms (id, user_id, title) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, chatRoom.id());
            pstmt.setString(2, chatRoom.userId());
            pstmt.setString(3, chatRoom.title());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB 저장 중 에러 발생", e);
        }
    }

    @Override
    public List<ChatRoom> findAllByUserId(String userId) {
        String sql = "SELECT id, user_id, title, created_at FROM chat_rooms WHERE user_id = ? ORDER BY created_at DESC";
        List<ChatRoom> chatRoomList = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ChatRoom room = new ChatRoom(
                            rs.getString("id"),
                            rs.getString("user_id"),
                            rs.getString("title"),
                            rs.getString("created_at")
                    );
                    chatRoomList.add(room);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB 조회 중 에러 발생", e);
        }
        return chatRoomList;
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM chat_rooms WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("DB 삭제 중 에러 발생", e);
        }
    }
}
