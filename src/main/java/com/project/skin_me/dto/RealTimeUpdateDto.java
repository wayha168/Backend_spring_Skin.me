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
public class RealTimeUpdateDto {
    private String updateId;
    private String entityType; // "ORDER", "PRODUCT", "INVENTORY", "PRICE"
    private String entityId;
    private String action; // "CREATE", "UPDATE", "DELETE"
    private Object data;
    private LocalDateTime timestamp;
    private String affectedUsers; // comma-separated user IDs or "ALL"
}
