package com.chat_app.mapper;

import com.chat_app.dto.response.UserResponse;
import com.chat_app.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "role.name", target = "role")
    UserResponse toResponse(User user);
}
