package com.project.skin_me.controller;

import com.project.skin_me.dto.ChatMessageDto;
import com.project.skin_me.dto.NotificationDto;
import com.project.skin_me.dto.RealTimeUpdateDto;
import com.project.skin_me.service.chatAI.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GeminiService geminiService;

    /**
     * Handle chat messages and send responses to all connected clients
     * Client sends to: /app/chat/message
     */
    @MessageMapping("/chat/message")
    @SendTo("/topic/chat")
    public ChatMessageDto handleChatMessage(ChatMessageDto message) {
        // Add metadata
        message.setId(UUID.randomUUID().toString());
        message.setTimestamp(LocalDateTime.now());
        message.setType("user");

        // Return message to all subscribers of /topic/chat
        return message;
    }

    /**
     * Handle chat queries with AI assistant
     * Client sends to: /app/chat/query
     * Response sent to specific user
     */
    @MessageMapping("/chat/query")
    @SendToUser("/topic/chat")
    public ChatMessageDto handleChatQuery(ChatMessageDto message) {
        try {
            // Get AI response
            String aiResponse = geminiService.askGemini(message.getContent());

            return ChatMessageDto.builder()
                    .id(UUID.randomUUID().toString())
                    .sender("assistant")
                    .content(aiResponse)
                    .type("assistant")
                    .timestamp(LocalDateTime.now())
                    .conversationId(message.getConversationId())
                    .build();
        } catch (Exception e) {
            return ChatMessageDto.builder()
                    .id(UUID.randomUUID().toString())
                    .sender("assistant")
                    .content("Sorry, I encountered an error. Please try again.")
                    .type("assistant")
                    .timestamp(LocalDateTime.now())
                    .conversationId(message.getConversationId())
                    .build();
        }
    }

    /**
     * Send notification to specific user
     * Internal service method - call from other services
     */
    public void sendUserNotification(String userId, NotificationDto notification) {
        notification.setId(UUID.randomUUID().toString());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus("UNREAD");

        messagingTemplate.convertAndSendToUser(
                userId,
                "/topic/notifications",
                notification);
    }

    /**
     * Send broadcast notification to all users
     * Internal service method - call from other services
     */
    public void sendBroadcastNotification(NotificationDto notification) {
        notification.setId(UUID.randomUUID().toString());
        notification.setCreatedAt(LocalDateTime.now());

        messagingTemplate.convertAndSend(
                "/topic/notifications",
                notification);
    }

    /**
     * Send real-time order update
     * Client sends to: /app/orders/update
     */
    @MessageMapping("/orders/update")
    @SendTo("/topic/orders")
    public RealTimeUpdateDto handleOrderUpdate(RealTimeUpdateDto update) {
        update.setUpdateId(UUID.randomUUID().toString());
        update.setTimestamp(LocalDateTime.now());
        update.setEntityType("ORDER");
        return update;
    }

    /**
     * Send real-time product update
     * Client sends to: /app/products/update
     */
    @MessageMapping("/products/update")
    @SendTo("/topic/products")
    public RealTimeUpdateDto handleProductUpdate(RealTimeUpdateDto update) {
        update.setUpdateId(UUID.randomUUID().toString());
        update.setTimestamp(LocalDateTime.now());
        update.setEntityType("PRODUCT");
        return update;
    }

    /**
     * Send real-time inventory update
     * Client sends to: /app/inventory/update
     */
    @MessageMapping("/inventory/update")
    @SendTo("/topic/inventory")
    public RealTimeUpdateDto handleInventoryUpdate(RealTimeUpdateDto update) {
        update.setUpdateId(UUID.randomUUID().toString());
        update.setTimestamp(LocalDateTime.now());
        update.setEntityType("INVENTORY");
        return update;
    }

}
