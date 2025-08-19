package com.chat_app.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "fcm_token")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FCMToken {
    @Id
    private String userId;
    private String token;
}
