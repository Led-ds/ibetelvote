package com.br.ibetelvote.application.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RefreshTokenResponse {
    String accessToken;
    String tokenType;
    Long expiresIn;
}
