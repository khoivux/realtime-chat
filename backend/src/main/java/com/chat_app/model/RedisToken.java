package com.chat_app.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("RedisToken")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RedisToken implements Serializable {
    String id;
    String accessToken;
    String refreshToken;
    String resetToken;
}
