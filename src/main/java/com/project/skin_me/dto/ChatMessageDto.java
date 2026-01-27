package com.project.skin_me.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private String id;
    private String sender;
    private String content;
    private String type; // "user", "assistant", "notification"
    private LocalDateTime timestamp;
    private String conversationId;
}
