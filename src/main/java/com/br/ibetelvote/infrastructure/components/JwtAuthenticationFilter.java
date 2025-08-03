package com.br.ibetelvote.infrastructure.components;

import com.br.ibetelvote.infrastructure.jwt.JwtService;
import com.br.ibetelvote.infrastructure.repositories.UserJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserJpaRepository userJpaRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Verificar se o header Authorization existe e tem o formato correto
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);

            // Validar token e verificar se é um access token
            if (!jwtService.validateToken(jwt) || !jwtService.isAccessToken(jwt)) {
                log.debug("Token inválido ou não é um access token");
                filterChain.doFilter(request, response);
                return;
            }

            final UUID userId = jwtService.extractUserId(jwt);

            // Se já existe autenticação no contexto, pular
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // Buscar usuário no banco
            UserDetails userDetails = userJpaRepository.findById(userId)
                    .filter(user -> user.getAtivo())
                    .orElse(null);

            if (userDetails != null) {
                // Criar token de autenticação
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Definir autenticação no contexto de segurança
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Usuário autenticado: {}", userDetails.getUsername());
            } else {
                log.debug("Usuário não encontrado ou inativo para o token");
            }

        } catch (Exception e) {
            log.error("Erro ao processar token JWT: {}", e.getMessage());
            // Limpar contexto de segurança em caso de erro
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Pular filtro para endpoints públicos
        return path.startsWith("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/refresh") ||
                path.startsWith("/api/v1/auth/validate") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                path.startsWith("/files/") ||
                path.equals("/favicon.ico");
    }
}