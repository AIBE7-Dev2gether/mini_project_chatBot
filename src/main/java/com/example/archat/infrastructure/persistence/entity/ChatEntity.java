package com.example.archat.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "chats")
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "owner", length = 20, nullable = false)
    private String owner;

    @Column(name = "user_id", length = 255)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoomEntity chatRoom;

    @Column(name = "model", length = 255)
    private String model;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ChatEntity() {
    }

    public ChatEntity(
            String message,
            String owner,
            String userId,
            ChatRoomEntity chatRoom,
            String model,
            LocalDateTime sentAt
    ) {
        this.message = message;
        this.owner = owner;
        this.userId = userId;
        this.chatRoom = chatRoom;
        this.model = model;
        this.sentAt = sentAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = createdAt == null ? LocalDateTime.now(ZoneOffset.UTC) : createdAt;
    }

    public String getMessage() {
        return message;
    }

    public String getOwner() {
        return owner;
    }

    public String getUserId() {
        return userId;
    }

    public ChatRoomEntity getChatRoom() {
        return chatRoom;
    }

    public String getModel() {
        return model;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
