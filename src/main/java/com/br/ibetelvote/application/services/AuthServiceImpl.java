package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.auth.dto.*;
import com.br.ibetelvote.application.mapper.AuthMapper;
import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.services.AuthService;
import com.br.ibetelvote.infrastructure.jwt.JwtService;
import com.br.ibetelvote.infrastructure.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserJpaRepository userJpaRepository;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Tentativa de login para email: {}", request.email());

        log.info("DEBUG - Senha recebida: '{}' (tamanho: {})",
                request.password(),
                request.password().length());

        try {
            // A chamada ao authenticationManager já valida a senha com o BCrypt
            // e verifica se o usuário existe e está ativo.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            log.warn("Falha na autenticação para email: {} - {}", request.email(), e.getMessage());
            // Lançar uma exceção mais específica para ser tratada pelo GlobalExceptionHandler
            if (e instanceof DisabledException) {
                throw new DisabledException("Usuário desativado ou inativo");
            }
            throw new BadCredentialsException("Credenciais inválidas");
        }

        // Se a autenticação foi bem-sucedida, busca o usuário para gerar os tokens
        User user = userJpaRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado após autenticação"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Login realizado com sucesso para usuário: {} ({})", user.getNome(), user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMinutes() * 60) // em segundos
                .user(authMapper.toUserProfileResponse(user))
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Tentativa de refresh token");

        String refreshToken = request.refreshToken();

        // Extrai o userId do token sem a necessidade de validar o token duas vezes
        UUID userId = jwtService.extractUserId(refreshToken);

        // Busca o usuário. A validação do token é feita dentro do JwtService.
        User user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário do refresh token não encontrado"));

        // Valida o refresh token e o estado do usuário em uma única verificação
        if (!jwtService.validateToken(refreshToken, user) || !user.getAtivo() || !jwtService.isRefreshToken(refreshToken)) {
            log.warn("Refresh token inválido ou expirado para usuário: {}", user.getEmail());
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }

        // Gera novo access token
        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("Refresh token realizado com sucesso para usuário: {}", user.getEmail());

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMinutes() * 60) // em segundos
                .build();
    }

    @Override
    @Cacheable(value = "userProfile", key = "#userId")
    public UserProfileResponse getCurrentUser(UUID userId) {
        log.debug("Buscando dados do usuário: {}", userId);
        User user = userJpaRepository.findById(userId)
                .filter(User::getAtivo) // Filtra apenas usuários ativos
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado ou desativado"));

        return authMapper.toUserProfileResponse(user);
    }

    @Override
    @CacheEvict(value = "userProfile", key = "#userId", allEntries = true)
    public void logout(String token) {
        // A lógica de logout aqui é apenas um log e a invalidação do token será feita no cliente
        // ou em uma lista de tokens inválidos (blacklist).
        // Se houver um cache de perfil, é importante limpá-lo.
        try {
            UUID userId = jwtService.extractUserId(token);
            log.info("Logout realizado para usuário: {}", userId);
        } catch (Exception e) {
            log.warn("Tentativa de logout com token inválido");
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            // Verifica o tipo do token e se ele é válido
            if (!jwtService.isAccessToken(token) || !jwtService.validateToken(token)) {
                return false;
            }

            // Extrai o ID e verifica se o usuário existe e está ativo no banco
            UUID userId = jwtService.extractUserId(token);
            return userJpaRepository.findById(userId)
                    .map(User::getAtivo)
                    .orElse(false);

        } catch (Exception e) {
            log.debug("Token inválido ou expirado: {}", e.getMessage());
            return false;
        }
    }
}