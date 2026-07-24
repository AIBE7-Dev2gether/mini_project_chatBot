package com.example.archat.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "SUPABASE_DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "AIVEN_MYSQL_HOST", matches = ".+")
class DatabaseMigrationPreflightTest {

    @Test
    void verifiesSourceDataCanBeSafelyMigratedToTheEmptyTarget() throws Exception {
        try (Connection source = sourceConnection(); Connection target = targetConnection()) {
            source.setReadOnly(true);

            long sourceAccounts = count(source, "SELECT COUNT(*) FROM account");
            long sourceRooms = count(source, "SELECT COUNT(*) FROM chat_rooms");
            long sourceChats = count(source, "SELECT COUNT(*) FROM chats");
            long nullRoomChats = count(source, "SELECT COUNT(*) FROM chats WHERE room_id IS NULL");
            long orphanRooms = count(source, """
                    SELECT COUNT(*)
                    FROM chat_rooms cr
                    LEFT JOIN account a ON a.user_id = cr.user_id
                    WHERE a.user_id IS NULL
                    """);
            long orphanChats = count(source, """
                    SELECT COUNT(*)
                    FROM chats c
                    LEFT JOIN chat_rooms cr ON cr.id = c.room_id
                    WHERE c.room_id IS NOT NULL AND cr.id IS NULL
                    """);
            long invalidTimestamps = countInvalidTimestamps(source);

            long targetAccounts = count(target, "SELECT COUNT(*) FROM account");
            long targetRooms = count(target, "SELECT COUNT(*) FROM chat_rooms");
            long targetChats = count(target, "SELECT COUNT(*) FROM chats");

            System.out.printf(
                    "MIGRATION_PREFLIGHT source(account=%d, rooms=%d, chats=%d) "
                            + "issues(nullRoom=%d, orphanRooms=%d, orphanChats=%d, invalidTimestamp=%d) "
                            + "target(account=%d, rooms=%d, chats=%d)%n",
                    sourceAccounts,
                    sourceRooms,
                    sourceChats,
                    nullRoomChats,
                    orphanRooms,
                    orphanChats,
                    invalidTimestamps,
                    targetAccounts,
                    targetRooms,
                    targetChats
            );

            assertThat(nullRoomChats).as("room_id가 없는 chat 수").isZero();
            assertThat(orphanRooms).as("account가 없는 chat_room 수").isZero();
            assertThat(orphanChats).as("chat_room이 없는 chat 수").isZero();
            assertThat(invalidTimestamps).as("파싱할 수 없는 chat timestamp 수").isZero();
            assertThat(targetAccounts + targetRooms + targetChats)
                    .as("데이터 이관 전 Aiven 대상 테이블의 전체 행 수")
                    .isZero();
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

    private long count(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private long countInvalidTimestamps(Connection source) throws SQLException {
        long invalid = 0;
        try (Statement statement = source.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT timestamp FROM chats")) {
            while (resultSet.next()) {
                String timestamp = resultSet.getString(1);
                if (timestamp == null || timestamp.isBlank()) {
                    continue;
                }
                try {
                    ZonedDateTime.parse(timestamp);
                } catch (DateTimeParseException exception) {
                    invalid++;
                }
            }
        }
        return invalid;
    }

    private String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " 환경변수가 필요합니다.");
        }
        return value;
    }
}
