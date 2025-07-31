package com.chat_app.config.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

@Slf4j(topic = "WEBSOCKET-EVENT-LISTENER")
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String ONLINE_PREFIX = "ONLINE:";

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if (user != null) {
            String userId = user.getName();
            redisTemplate.opsForValue().set(ONLINE_PREFIX + userId, "true");
            String value = redisTemplate.opsForValue().get(ONLINE_PREFIX + userId);
            log.info("REDIS STORED VALUE {}: {}", ONLINE_PREFIX + userId, value);
            
            // Gửi notification cho tất cả user về user online
            messagingTemplate.convertAndSend("/topic/online-status", Map.of(
                "type", "USER_ONLINE",
                "userId", userId
            ));
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            String userId = user.getName();
            redisTemplate.delete(ONLINE_PREFIX + userId);
            log.info("REDIS DELETED: {}", userId);
            
            // Gửi notification cho tất cả user về user offline
            messagingTemplate.convertAndSend("/topic/online-status", Map.of(
                "type", "USER_OFFLINE",
                "userId", userId
            ));
        }
    }
}
