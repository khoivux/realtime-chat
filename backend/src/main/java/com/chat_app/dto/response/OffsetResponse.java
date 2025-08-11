package com.chat_app.dto.response;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OffsetResponse<T> {
    private List<T> content;
    private int nextOffset;
}
