package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.auth.dto.*;
import com.br.ibetelvote.domain.services.AuthService;
import com.br.ibetelvote.infrastructure.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Autenticação", description = "Endpoints para autenticação e autorização")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica um usuário e retorna tokens de acesso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuário desativado")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Email ou senha incorretos");
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renova o token de acesso usando o refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido"),
            @ApiResponse(responseCode = "401", description = "Refresh token expirado")
    })
    public ResponseEntity<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Dados do usuário logado", description = "Retorna os dados do usuário atual")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Usuário desativado")
    })
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        // Extrair userId do token via request
        String token = extractTokenFromRequest();
        UUID userId = jwtService.extractUserId(token);

        UserProfileResponse response = authService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Fazer logout", description = "Invalida o token atual do usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar token", description = "Verifica se um token é válido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Void> validateToken(@RequestParam String token) {
        if (authService.isTokenValid(token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // Métodos auxiliares
    private String extractTokenFromRequest() {
        // Este método será implementado via interceptor ou filter
        // Por agora, retornamos uma implementação básica
        return getCurrentHttpRequest()
                .map(this::extractTokenFromRequest)
                .orElseThrow(() -> new IllegalStateException("Não foi possível extrair o token"));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        throw new IllegalArgumentException("Token não encontrado no header Authorization");
    }

    private java.util.Optional<HttpServletRequest> getCurrentHttpRequest() {
        try {
            return java.util.Optional.of(
                    ((org.springframework.web.context.request.ServletRequestAttributes)
                            org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes())
                            .getRequest()
            );
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    // Exception Handlers
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthErrorResponse> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        AuthErrorResponse error = AuthErrorResponse.of(
                "INVALID_CREDENTIALS",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        AuthErrorResponse error = AuthErrorResponse.of(
                "INVALID_REQUEST",
                e.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<AuthErrorResponse> handleUserNotFound(
            org.springframework.security.core.userdetails.UsernameNotFoundException e,
            HttpServletRequest request) {
        AuthErrorResponse error = AuthErrorResponse.of(
                "USER_NOT_FOUND",
                "Usuário não encontrado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<AuthErrorResponse> handleDisabledUser(
            org.springframework.security.authentication.DisabledException e,
            HttpServletRequest request) {
        AuthErrorResponse error = AuthErrorResponse.of(
                "USER_DISABLED",
                "Usuário desativado",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}