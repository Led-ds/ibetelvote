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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Define o SecurityFilterChain, que é o ponto principal da configuração de segurança.
     * @param http Objeto HttpSecurity para configurar a segurança web.
     * @return O SecurityFilterChain configurado.
     * @throws Exception se ocorrer um erro na configuração.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // === ENDPOINTS PÚBLICOS (ORDEM ESPECÍFICA → GERAL) ===

                        // Autenticação básica
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/validate").permitAll()

                        //AUTO-CADASTRO PÚBLICO - DEVE VIR ANTES DE QUALQUER MATCHER GERAL
                        .requestMatchers(HttpMethod.POST, "/api/v1/auto-cadastro/validar-membro").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auto-cadastro/criar-usuario").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auto-cadastro/verificar-elegibilidade").permitAll()

                        // Debug endpoints (remover em produção)
                        .requestMatchers("/debug/**").permitAll()

                        // Documentação e ferramentas de desenvolvimento
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()

                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // Arquivos estáticos
                        .requestMatchers(HttpMethod.GET, "/api/v1/files/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/files/**").permitAll()

                        //ENDPOINT DE ERRO - DEVE SER PÚBLICO
                        .requestMatchers("/error").permitAll()

                        // === ENDPOINTS AUTENTICADOS ===

                        // Endpoints de autenticação autorizados
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/logout").authenticated()

                        //AUTO-CADASTRO AUTENTICADO - PERFIL PRÓPRIO
                        .requestMatchers(HttpMethod.GET, "/api/v1/auto-cadastro/meu-perfil").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/auto-cadastro/meu-perfil").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/auto-cadastro/meu-perfil/foto").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/auto-cadastro/meu-perfil/foto").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // === ELEIÇÕES - ORDEM IMPORTA! ===
                        // Endpoints específicos primeiro (mais restritivos)
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/ativa").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/abertas").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/encerradas").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/futuras").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/recentes").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/can-activate").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/is-open").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/stats/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // Operações de controle
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/ativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/desativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/encerrar").hasRole("ADMINISTRADOR")

                        // CRUD básico
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eleicoes/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/eleicoes/**").hasRole("ADMINISTRADOR")

                        // === MEMBROS ===
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros/**").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/membros").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/membros/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/membros/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/membros/*/foto").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // === CARGOS ===
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos/**").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/cargos").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/cargos").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cargos/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cargos/**").hasRole("ADMINISTRADOR")

                        // === CANDIDATOS ===
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos/**").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/candidatos").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/candidatos").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/candidatos/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/candidatos/**").hasRole("ADMINISTRADOR")

                        // === VOTAÇÃO ===
                        .requestMatchers(HttpMethod.GET, "/api/v1/votos/**").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/votos/votar").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/votos/validar").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")

                        // === USUÁRIOS ===
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMINISTRADOR")

                        // === UPLOAD DE ARQUIVOS ===
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // Qualquer outra requisição deve ser autenticada
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .build();
    }

    /**
     * Configuração para o CORS (Cross-Origin Resource Sharing).
     * @return Um CorsConfigurationSource configurado.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        configuration.setExposedHeaders(List.of(
                "Content-Type",
                "Content-Length",
                "Accept-Ranges",
                "Content-Range"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Define o provedor de autenticação com o UserDetailsService e o PasswordEncoder.
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
     * @return Uma instância de PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); //Força padrão (10) - CORRIGIDO
    }
}