package com.example.archat.infrastructure.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    
    // 톰캣 플러그인을 통해 환경변수(System Environment Variables)로 등록된 값을 가져옵니다.
    private static final String URL = System.getenv("SUPABASE_DB_URL");
    private static final String USER = System.getenv("SUPABASE_DB_USER");
    private static final String PASSWORD = System.getenv("SUPABASE_DB_PASSWORD");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver 로드 실패", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        // 환경변수가 제대로 로드되지 않았을 경우를 대비한 기본적인 방어 코드 (디버깅 용도)
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("데이터베이스 접속 정보가 환경변수에 제대로 설정되지 않았습니다.");
        }
        
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
