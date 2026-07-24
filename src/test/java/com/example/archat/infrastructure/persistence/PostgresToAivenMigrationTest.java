package com.example.archat.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "RUN_DATA_MIGRATION", matches = "true")
class PostgresToAivenMigrationTest {

    @Test
    void migratesAllApplicationDataInOneTargetTransaction() throws Exception {
        try (Connection source = sourceConnection(); Connection target = targetConnection()) {
            source.setAutoCommit(false);
            source.setReadOnly(true);
            source.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            target.setAutoCommit(false);

            try {
                assertEmptyTarget(target);

                int accounts = migrateAccounts(source, target);
                int rooms = migrateRooms(source, target);
                int chats = migrateChats(source, target);

                assertThat(count(target, "SELECT COUNT(*) FROM account")).isEqualTo(accounts);
                assertThat(count(target, "SELECT COUNT(*) FROM chat_rooms")).isEqualTo(rooms);
                assertThat(count(target, "SELECT COUNT(*) FROM chats")).isEqualTo(chats);
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

                target.commit();
                System.out.printf(
                        "DATA_MIGRATION_COMMITTED account=%d, rooms=%d, chats=%d%n",
                        accounts,
                        rooms,
                        chats
                );
            } catch (Exception exception) {
                target.rollback();
                throw exception;
            } finally {
                source.rollback();
            }
        }
    }

    private int migrateAccounts(Connection source, Connection target) throws SQLException {
        String sourceSql = "SELECT user_id, email, created_at, updated_at FROM account ORDER BY user_id";
        String targetSql = """
                INSERT INTO account (user_id, email, created_at, updated_at)
                VALUES (?, ?, ?, ?)
                """;
        int count = 0;
        try (Statement read = source.createStatement();
             ResultSet rows = read.executeQuery(sourceSql);
             PreparedStatement write = target.prepareStatement(targetSql)) {
            while (rows.next()) {
                write.setString(1, rows.getString("user_id"));
                write.setString(2, rows.getString("email"));
                write.setTimestamp(3, utcTimestamp(rows, "created_at"));
                write.setTimestamp(4, utcTimestamp(rows, "updated_at"));
                write.addBatch();
                count++;
            }
            write.executeBatch();
        }
        return count;
    }

    private int migrateRooms(Connection source, Connection target) throws SQLException {
        boolean hasUpdatedAt = hasColumn(source, "chat_rooms", "updated_at");
        String sourceSql = "SELECT id, user_id, title, created_at"
                + (hasUpdatedAt ? ", updated_at" : "")
                + " FROM chat_rooms ORDER BY id";
        String targetSql = """
                INSERT INTO chat_rooms (id, user_id, title, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        int count = 0;
        try (Statement read = source.createStatement();
             ResultSet rows = read.executeQuery(sourceSql);
             PreparedStatement write = target.prepareStatement(targetSql)) {
            while (rows.next()) {
                Timestamp createdAt = utcTimestamp(rows, "created_at");
                write.setString(1, rows.getString("id"));
                write.setString(2, rows.getString("user_id"));
                write.setString(3, rows.getString("title"));
                write.setTimestamp(4, createdAt);
                write.setTimestamp(5, hasUpdatedAt ? utcTimestamp(rows, "updated_at") : createdAt);
                write.addBatch();
                count++;
            }
            write.executeBatch();
        }
        return count;
    }

    private int migrateChats(Connection source, Connection target) throws SQLException {
        String sourceSql = """
                SELECT id, message, owner, user_id, room_id, model, timestamp, created_at
                FROM chats
                ORDER BY id
                """;
        String targetSql = """
                INSERT INTO chats (id, message, owner, user_id, room_id, model, sent_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        int count = 0;
        try (Statement read = source.createStatement();
             ResultSet rows = read.executeQuery(sourceSql);
             PreparedStatement write = target.prepareStatement(targetSql)) {
            while (rows.next()) {
                write.setLong(1, rows.getLong("id"));
                write.setString(2, rows.getString("message"));
                write.setString(3, rows.getString("owner"));
                write.setString(4, rows.getString("user_id"));
                write.setString(5, rows.getString("room_id"));
                write.setString(6, rows.getString("model"));
                write.setTimestamp(7, parseChatTimestamp(rows.getString("timestamp")));
                write.setTimestamp(8, utcTimestamp(rows, "created_at"));
                write.addBatch();
                count++;
            }
            write.executeBatch();
        }
        return count;
    }

    private void assertEmptyTarget(Connection target) throws SQLException {
        long rows = count(target, "SELECT COUNT(*) FROM account")
                + count(target, "SELECT COUNT(*) FROM chat_rooms")
                + count(target, "SELECT COUNT(*) FROM chats");
        assertThat(rows).as("Aiven 대상 테이블의 기존 데이터 수").isZero();
    }

    private boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        try (ResultSet columns = metadata.getColumns(null, null, table, column)) {
            return columns.next();
        }
    }

    private Timestamp utcTimestamp(ResultSet rows, String column) throws SQLException {
        Object value = rows.getObject(column);
        if (value == null) {
            return null;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return Timestamp.valueOf(offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        }
        if (value instanceof Timestamp timestamp) {
            return Timestamp.valueOf(timestamp.toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime());
        }
        if (value instanceof LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        }
        return Timestamp.valueOf(OffsetDateTime.parse(value.toString())
                .withOffsetSameInstant(ZoneOffset.UTC)
                .toLocalDateTime());
    }

    private Timestamp parseChatTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Timestamp.valueOf(ZonedDateTime.parse(value)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime());
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
}
