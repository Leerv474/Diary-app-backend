package com.leerv.diary.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginDto {
    @NotNull(message = "email is mandatory")
    @NotBlank(message = "email is mandatory")
    @Email
    private String email;

    @NotNull(message = "password is mandatory")
    @NotBlank(message = "password is mandatory")
    @Pattern(regexp = "^[\\x00-\\x7F]+$", message = "password should not contain non-latin characters")
    @Size(min = 8, message = "Password should be 8 characters long minimum")
    private String password;
}