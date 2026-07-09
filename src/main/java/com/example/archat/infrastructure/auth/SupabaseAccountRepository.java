package com.example.archat.infrastructure.auth;

import com.example.archat.domain.auth.AuthUser;
import com.example.archat.domain.auth.AccountRepository;
import com.example.archat.infrastructure.db.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SupabaseAccountRepository implements AccountRepository {

    private SupabaseAccountRepository() {
    }

    private static final SupabaseAccountRepository instance = new SupabaseAccountRepository();

    public static SupabaseAccountRepository getInstance() {
        return instance;
    }

    @Override
    public void upsert(AuthUser authUser) {
        String sql = """
                INSERT INTO account (user_id, email, created_at, updated_at)
                VALUES (?, ?, NOW(), NOW())
                ON CONFLICT (user_id)
                DO UPDATE SET email = EXCLUDED.email, updated_at = NOW()
                """;

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, authUser.userId());
            pstmt.setString(2, authUser.email());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("account upsert 중 에러 발생", e);
        }
    }
}
