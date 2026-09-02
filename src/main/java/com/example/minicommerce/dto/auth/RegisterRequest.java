package com.example.minicommerce.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank
    private String username;
    @NotBlank
    @Email
    private  String mail;
    @NotBlank
    @Size(min = 8, max = 20)
    private String password;

}
