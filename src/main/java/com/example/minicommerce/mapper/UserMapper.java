package com.example.minicommerce.mapper;

import com.example.minicommerce.dto.UserResponse;
import com.example.minicommerce.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    List<UserResponse> toResponseList(List<User> user);

}
