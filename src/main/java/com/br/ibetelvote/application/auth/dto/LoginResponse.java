package com.br.ibetelvote.application.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class LoginResponse {
    String accessToken;
    String refreshToken;
    String tokenType;
    Long expiresIn;
    UserProfileResponse user;
}