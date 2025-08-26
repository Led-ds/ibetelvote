package com.br.ibetelvote.infrastructure.config;

import com.br.ibetelvote.infrastructure.components.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuração de Segurança Refatorada - Sistema IBetel Vote
 *
 * Atualizada com base nos controllers atuais
 * Permissões consistentes com @PreAuthorize
 * Todos os endpoints mapeados
 * Ordem correta dos matchers (específico → geral)
 *
 * HIERARQUIA DE ROLES:
 * - MEMBRO: Acesso básico (votar, ver eleições ativas)
 * - UTILIZADOR_PRO: Gestão intermediária (relatórios, consultas)
 * - ADMINISTRADOR: Controle total (CRUD, configurações)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ===============================================
                        // ENDPOINTS PÚBLICOS (SEM AUTENTICAÇÃO)
                        // ===============================================

                        // AUTENTICAÇÃO BÁSICA
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/validate").permitAll()

                        // AUTO-CADASTRO PÚBLICO (DEVE VIR ANTES DE MATCHERS GERAIS)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auto-cadastro/validar-membro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auto-cadastro/criar-usuario").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auto-cadastro/verificar-elegibilidade").permitAll()

                        // ARQUIVOS ESTÁTICOS (IMAGENS, DOCUMENTOS)
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/files/**").permitAll()

                        // FERRAMENTAS DE DESENVOLVIMENTO
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/favicon.ico", "/error").permitAll()

                        // ===============================================
                        // ENDPOINTS AUTENTICADOS
                        // ===============================================

                        // AUTENTICAÇÃO COM TOKEN
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()

                        // ===============================================
                        // AUTO-CADASTRO (PERFIL PRÓPRIO)
                        // ===============================================
                        .requestMatchers(HttpMethod.GET, "/api/v1/auto-cadastro/meu-perfil").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/auto-cadastro/meu-perfil").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auto-cadastro/meu-perfil/foto").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/auto-cadastro/meu-perfil/foto").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // ===============================================
                        // ELEIÇÕES (ORDEM ESPECÍFICA → GERAL)
                        // ===============================================

                        //ESTATÍSTICAS (PRIMEIRO - MAIS ESPECÍFICO)
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/stats/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // CONSULTAS ESPECÍFICAS
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/ativa").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/abertas").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/encerradas").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/futuras").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/recentes").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/com-candidatos").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/buscar").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // VALIDAÇÕES E CONFIGURAÇÕES (ADMIN ONLY)
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/validacao").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/can-activate").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eleicoes/*/configuracoes").hasRole("ADMINISTRADOR")

                        // CONTROLE DE ESTADO (ADMIN ONLY)
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/ativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/desativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/encerrar").hasRole("ADMINISTRADOR")

                        // CONSULTAS GERAIS
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/is-open").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/stats").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // CRUD BÁSICO
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eleicoes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eleicoes/**").hasRole("ADMINISTRADOR")

                        // ===============================================
                        // CANDIDATOS (ORDEM ESPECÍFICA → GERAL)
                        // ===============================================

                        // ESTATÍSTICAS E RELATÓRIOS
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/stats/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // CONSULTAS ESPECÍFICAS
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/pendentes-aprovacao").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/eleicao/*/listagem").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/eleicao/*/listagem/paginada").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/eleicao/*/elegiveis").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/eleicao/*/sem-numero").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/eleicao/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/cargo/*/aprovados").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/cargo/*/eleicao/*/ranking").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/cargo/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/membro/*").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/numero/*/eleicao/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/search").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // VALIDAÇÕES
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/exists/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/*/can-delete").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/*/elegibilidade").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // APROVAÇÃO E CONTROLE (ADMIN ONLY)
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos/*/aprovar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos/*/reprovar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos/aprovar-lote").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos/*/ativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos/*/desativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/candidatos/*/numero").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/candidatos/*/cargo-pretendido").hasRole("ADMINISTRADOR")

                        // GESTÃO DE FOTOS (ADMIN ONLY)
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos/*/foto-campanha").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/candidatos/*/foto-campanha").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/*/foto-campanha").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // BUSCA AVANÇADA
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos/filtros").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // CRUD BÁSICO
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/*/with-photo").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/candidatos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/candidatos/**").hasRole("ADMINISTRADOR")

                        // ===============================================
                        // CARGOS
                        // ===============================================

                        // ESTATÍSTICAS
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/stats/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // CONSULTAS ESPECÍFICAS
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/ativos").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/ativos/page").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/inativos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/disponiveis").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/search").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/basic-info").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/all").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // VALIDAÇÕES
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/exists/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/disponivel/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/*/can-delete").hasRole("ADMINISTRADOR")

                        // CONTROLE DE ESTADO
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/cargos/*/ativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/cargos/*/desativar").hasRole("ADMINISTRADOR")

                        // CRUD BÁSICO
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/cargos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cargos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cargos/**").hasRole("ADMINISTRADOR")

                        // ===============================================
                        // MEMBROS (ORDEM ESPECÍFICA → GERAL)
                        // ===============================================

                        // ESTATÍSTICAS
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/stats/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // CONSULTAS ESPECÍFICAS
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/ativos").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/ativos/page").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/inativos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/cargo/*").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/sem-cargo").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/elegiveis/*").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/aptos-votacao").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/podem-criar-usuario").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/listagem").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/basic-info").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/all").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // BUSCA
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/search/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/membros/filtros").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // VALIDAÇÕES
                        .requestMatchers(HttpMethod.POST, "/api/v1/membros/validar").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/*/elegibilidade").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/disponivel/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/*/can-delete").hasRole("ADMINISTRADOR")

                        // CONTROLE DE ESTADO
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/membros/*/ativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/membros/*/desativar").hasRole("ADMINISTRADOR")

                        // GESTÃO DE CARGOS
                        .requestMatchers(HttpMethod.PUT, "/api/v1/membros/*/cargo").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/membros/*/cargo").hasRole("ADMINISTRADOR")

                        // GESTÃO DE USUÁRIOS
                        .requestMatchers(HttpMethod.PUT, "/api/v1/membros/*/usuario/*").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/membros/*/usuario").hasRole("ADMINISTRADOR")

                        // GESTÃO DE FOTOS
                        .requestMatchers(HttpMethod.PUT, "/api/v1/membros/*/foto").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/membros/*/foto").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/*/foto").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // PERFIL
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/*/profile").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/membros/*/profile").hasAnyRole("MEMBRO", "ADMINISTRADOR")

                        // CRUD BÁSICO
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/membros").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/membros/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/membros/**").hasRole("ADMINISTRADOR")

                        // ===============================================
                        // VOTAÇÃO (ORDEM ESPECÍFICA → GERAL)
                        // ===============================================

                        // ESTATÍSTICAS E RELATÓRIOS
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/stats/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/estatisticas").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/estatisticas-detalhadas").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/resultados").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/cargo-pretendido/*/ranking").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/progresso").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/cargo-pretendido/*/estatisticas").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // AUDITORIA E SEGURANÇA (ADMIN ONLY)
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/auditoria").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/seguranca").hasRole("ADMINISTRADOR")

                        // CONSULTAS POR ENTIDADE
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/paginados").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/total").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/cargo-pretendido/*/total").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/cargo-pretendido/*").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/candidato/*/total").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/candidato/*").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // CONSULTAS POR MEMBRO
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/membro/*/eleicao/*/ja-votou").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/membro/*/cargo-pretendido/*/eleicao/*/ja-votou").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/membro/*/elegivel").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/membro/*").hasAnyRole("ADMINISTRADOR", "MEMBRO")

                        // VALIDAÇÕES
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/eleicao/*/disponivel").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/votos/validar").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // OPERAÇÃO PRINCIPAL
                        .requestMatchers(HttpMethod.POST, "/api/v1/votos/votar").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // BUSCA AVANÇADA
                        .requestMatchers(HttpMethod.POST, "/api/v1/votos/buscar").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // ENDPOINTS DEPRECATED (COMPATIBILIDADE)
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/cargo/*/total").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/cargo/*").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // ===============================================
                        // USUÁRIOS (ADMIN ONLY)
                        // ===============================================
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/users/**").hasRole("ADMINISTRADOR")

                        // ===============================================
                        // GESTÃO DE ARQUIVOS (ADMIN/UTILIZADOR_PRO)
                        // ===============================================
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/files/**").hasRole("ADMINISTRADOR")

                        // ===============================================
                        // QUALQUER OUTRA REQUISIÇÃO
                        // ===============================================
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .build();
    }

    /**
     * Configuração para o CORS (Cross-Origin Resource Sharing).
     *
     * Permite requisições de qualquer origem
     * Suporta todos os métodos HTTP necessários
     * Headers personalizados expostos
     * Cache configurado para melhor performance
     *
     * @return Um CorsConfigurationSource configurado.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache por 1 hora

        configuration.setExposedHeaders(List.of(
                "Content-Type",
                "Content-Length",
                "Accept-Ranges",
                "Content-Range",
                "Authorization",
                "X-Total-Count"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Define o provedor de autenticação com o UserDetailsService e o PasswordEncoder.
     *
     * Integração com UserDetailsService customizado
     * BCrypt para criptografia de senhas
     *
     * @return O AuthenticationProvider configurado.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Fornece o AuthenticationManager, que é responsável por gerenciar a autenticação.
     *
     * @param config A configuração de autenticação do Spring.
     * @return O AuthenticationManager configurado.
     * @throws Exception se ocorrer um erro na configuração.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Define o bean para o PasswordEncoder, utilizando BCrypt.
     *
     * BCrypt com força padrão (10 rounds)
     * Segurança adequada para produção
     *
     * @return Uma instância de PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Força padrão (10 rounds)
    }
}