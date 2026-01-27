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
public class NotificationDto {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // "ORDER", "DELIVERY", "PRODUCT", "PROMOTION"
    private String status; // "UNREAD", "READ"
    private LocalDateTime createdAt;
    private String actionUrl;
}
