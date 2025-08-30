package com.br.ibetelvote.infrastructure.jwt;

import com.br.ibetelvote.application.auth.dto.JwtClaimsResponse;
import com.br.ibetelvote.domain.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {
    private final SecretKey secretKey;
    private final long accessTokenExpirationMinutes;
    private final long refreshTokenExpirationDays;
    private final String issuer;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.access-token-expiration-minutes:60}") long accessTokenExpirationMinutes,
            @Value("${app.security.jwt.refresh-token-expiration-days:7}") long refreshTokenExpirationDays,
            @Value("${app.security.jwt.issuer:ibetelvote}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMinutes = accessTokenExpirationMinutes;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
        this.issuer = issuer;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("email", user.getEmail())
                .claim("nome", user.getNome())
                .claim("role", user.getRole().name())
                .claim("ativo", user.getAtivo())
                .claim("type", "access")
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenExpirationDays, ChronoUnit.DAYS);

        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("email", user.getEmail())
                .claim("type", "refresh")
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Token JWT não suportado: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Assinatura JWT inválida: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token JWT vazio ou nulo: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Valida um token JWT e verifica se o token pertence a um usuário específico.
     * Esta validação é mais completa e deve ser usada em conjunto com o token de refresh.
     * @param token O token a ser validado.
     * @param user A entidade do usuário ao qual o token deve pertencer.
     * @return true se o token for válido e o subject (ID do usuário) corresponder, false caso contrário.
     */
    public boolean validateToken(String token, User user) {
        if (!validateToken(token)) {
            return false;
        }

        try {
            UUID userIdInToken = extractUserId(token);
            return userIdInToken != null && userIdInToken.equals(user.getId());
        } catch (Exception e) {
            log.warn("Falha na validação do token JWT com usuário: {}", e.getMessage());
            return false;
        }
    }

    public JwtClaimsResponse extractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return JwtClaimsResponse.builder()
                    .userId(UUID.fromString(claims.getSubject()))
                    .email(claims.get("email", String.class))
                    .nome(claims.get("nome", String.class))
                    .role(com.br.ibetelvote.domain.entities.enums.UserRole.valueOf(
                            claims.get("role", String.class)
                    ))
                    .ativo(claims.get("ativo", Boolean.class))
                    .build();
        } catch (Exception e) {
            log.error("Erro ao extrair claims do token: {}", e.getMessage());
            throw new IllegalArgumentException("Token inválido", e);
        }
    }

    public UUID extractUserId(String token) {
        return extractClaims(token).getUserId();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getEmail();
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "access".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessTokenExpirationMinutes() {
        return accessTokenExpirationMinutes;
    }

    public long getRefreshTokenExpirationDays() {
        return refreshTokenExpirationDays;
    }
}
