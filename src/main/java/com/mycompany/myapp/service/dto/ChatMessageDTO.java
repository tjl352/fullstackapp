package com.mycompany.myapp.service.dto;

import com.mycompany.myapp.domain.ChatMessage;
import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for chat messages returned to the client.
 */
public class ChatMessageDTO implements Serializable {

    private Long id;
    private String role;
    private String content;
    private Instant timestamp;

    public ChatMessageDTO() {}

    public ChatMessageDTO(Long id, String role, String content, Instant timestamp) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static ChatMessageDTO fromEntity(ChatMessage chatMessage) {
        return new ChatMessageDTO(chatMessage.getId(), chatMessage.getRole(), chatMessage.getContent(), chatMessage.getTimestamp());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
