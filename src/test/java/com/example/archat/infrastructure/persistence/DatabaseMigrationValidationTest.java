package com.example.archat.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "SUPABASE_DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "AIVEN_MYSQL_HOST", matches = ".+")
class DatabaseMigrationValidationTest {

    @Test
    void sourceAndTargetContainTheSameApplicationData() throws Exception {
        try (Connection source = sourceConnection(); Connection target = targetConnection()) {
            source.setReadOnly(true);

            assertThat(loadAccounts(target)).isEqualTo(loadAccounts(source));
            assertThat(loadRooms(target)).isEqualTo(loadRooms(source));
            assertThat(loadTargetChats(target)).isEqualTo(loadSourceChats(source));

            assertThat(count(target, """
                    SELECT COUNT(*)
                    FROM chat_rooms cr
                    LEFT JOIN account a ON a.user_id = cr.user_id
                    WHERE a.user_id IS NULL
                    """)).isZero();
            assertThat(count(target, """
                    SELECT COUNT(*)
                    FROM chats c
                    LEFT JOIN chat_rooms cr ON cr.id = c.room_id
                    WHERE cr.id IS NULL
                    """)).isZero();

            System.out.printf(
                    "DATA_MIGRATION_VALIDATED account=%d, rooms=%d, chats=%d%n",
                    loadAccounts(target).size(),
                    loadRooms(target).size(),
                    loadTargetChats(target).size()
            );
        }
    }

    private Map<String, AccountData> loadAccounts(Connection connection) throws SQLException {
        Map<String, AccountData> values = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT user_id, email FROM account ORDER BY user_id")) {
            while (rows.next()) {
                values.put(rows.getString("user_id"), new AccountData(rows.getString("email")));
            }
        }
        return values;
    }

    private Map<String, RoomData> loadRooms(Connection connection) throws SQLException {
        Map<String, RoomData> values = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery(
                     "SELECT id, user_id, title FROM chat_rooms ORDER BY id"
             )) {
            while (rows.next()) {
                values.put(rows.getString("id"), new RoomData(
                        rows.getString("user_id"),
                        rows.getString("title")
                ));
            }
        }
        return values;
    }

    private Map<Long, ChatData> loadSourceChats(Connection connection) throws SQLException {
        Map<Long, ChatData> values = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("""
                     SELECT id, message, owner, user_id, room_id, model, timestamp
                     FROM chats
                     ORDER BY id
                     """)) {
            while (rows.next()) {
                String timestamp = rows.getString("timestamp");
                LocalDateTime sentAt = timestamp == null || timestamp.isBlank()
                        ? null
                        : roundToMicros(ZonedDateTime.parse(timestamp)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime());
                values.put(rows.getLong("id"), chatData(rows, sentAt));
            }
        }
        return values;
    }

    private Map<Long, ChatData> loadTargetChats(Connection connection) throws SQLException {
        Map<Long, ChatData> values = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("""
                     SELECT id, message, owner, user_id, room_id, model, sent_at
                     FROM chats
                     ORDER BY id
                     """)) {
            while (rows.next()) {
                Timestamp timestamp = rows.getTimestamp("sent_at");
                LocalDateTime sentAt = timestamp == null ? null : timestamp.toLocalDateTime();
                values.put(rows.getLong("id"), chatData(rows, sentAt));
            }
        }
        return values;
    }

    private ChatData chatData(ResultSet rows, LocalDateTime sentAt) throws SQLException {
        return new ChatData(
                rows.getString("message"),
                rows.getString("owner"),
                rows.getString("user_id"),
                rows.getString("room_id"),
                rows.getString("model"),
                sentAt
        );
    }

    private LocalDateTime roundToMicros(LocalDateTime value) {
        LocalDateTime rounded = value.plusNanos(500);
        return rounded.withNano((rounded.getNano() / 1_000) * 1_000);
    }

    private long count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private Connection sourceConnection() throws SQLException {
        return DriverManager.getConnection(
                required("SUPABASE_DB_URL"),
                required("SUPABASE_DB_USER"),
                required("SUPABASE_DB_PASSWORD")
        );
    }

    private Connection targetConnection() throws SQLException {
        String sslMode = System.getenv().getOrDefault("AIVEN_MYSQL_SSL_MODE", "REQUIRED");
        String url = "jdbc:mysql://%s:%s/%s?sslMode=%s&serverTimezone=UTC&characterEncoding=UTF-8".formatted(
                required("AIVEN_MYSQL_HOST"),
                required("AIVEN_MYSQL_PORT"),
                required("AIVEN_MYSQL_DATABASE"),
                sslMode
        );
        return DriverManager.getConnection(
                url,
                required("AIVEN_MYSQL_USER"),
                required("AIVEN_MYSQL_PASSWORD")
        );
    }

    private String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 환경변수가 필요합니다.");
        }
        return value;
    }

    private record AccountData(String email) {
    }

    private record RoomData(String userId, String title) {
    }

    private record ChatData(
            String message,
            String owner,
            String userId,
            String roomId,
            String model,
            LocalDateTime sentAt
    ) {
    }
}
