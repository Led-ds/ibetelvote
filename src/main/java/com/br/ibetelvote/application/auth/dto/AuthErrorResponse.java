package com.br.ibetelvote.application.auth.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class AuthErrorResponse {
    String error;
    String message;
    String timestamp;
    String path;

    public static AuthErrorResponse of(String error, String message, String path) {
        return AuthErrorResponse.builder()
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .path(path)
                .build();
    }
}