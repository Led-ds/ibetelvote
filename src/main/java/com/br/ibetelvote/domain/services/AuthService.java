package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.auth.dto.LoginRequest;
import com.br.ibetelvote.application.auth.dto.LoginResponse;
import com.br.ibetelvote.application.auth.dto.RefreshTokenRequest;
import com.br.ibetelvote.application.auth.dto.RefreshTokenResponse;
import com.br.ibetelvote.application.auth.dto.UserProfileResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Interface de serviço para o domínio de Autenticação.
 * Define o contrato das operações de autenticação e gerenciamento de sessão,
 * seguindo o padrão de design de domínio.
 */
public interface AuthService {

    /**
     * Realiza a autenticação do usuário com email e senha.
     * @param request DTO com credenciais de login.
     * @return DTO com tokens de acesso e refresh.
     */
    @Transactional
    LoginResponse login(LoginRequest request);

    /**
     * Gera um novo token de acesso a partir de um token de refresh válido.
     * @param request DTO com o refresh token.
     * @return DTO com o novo token de acesso.
     */
    @Transactional
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * Busca os dados do perfil do usuário logado.
     * O resultado desta consulta é armazenado em cache para otimização.
     * @param userId ID do usuário.
     * @return DTO com os dados do perfil do usuário.
     */
    @Cacheable(value = "userProfile", key = "#userId")
    UserProfileResponse getCurrentUser(UUID userId);

    /**
     * Invalida o token de acesso, simulando um logout.
     * @param token O token de acesso a ser invalidado.
     */
    void logout(String token);

    /**
     * Verifica a validade de um token de acesso, incluindo se o usuário está ativo.
     * @param token O token de acesso.
     * @return true se o token for válido e o usuário estiver ativo, false caso contrário.
     */
    boolean isTokenValid(String token);
}