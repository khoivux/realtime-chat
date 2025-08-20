package com.chat_app.config.websocket;

import com.chat_app.constant.Constants;
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

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if (user != null) {
            String userId = user.getName();
            redisTemplate.opsForValue().set(Constants.ONLINE_PREFIX + userId, "true");
            log.info("REDIS STORED VALUE {}: {}", Constants.ONLINE_PREFIX + userId,
                    redisTemplate.opsForValue().get(Constants.ONLINE_PREFIX + userId));

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
            redisTemplate.delete(Constants.ONLINE_PREFIX + userId);
            log.info("REDIS DELETED: {}", userId);

            messagingTemplate.convertAndSend("/topic/online-status", Map.of(
                "type", "USER_OFFLINE",
                "userId", userId
            ));
        }
    }
}
