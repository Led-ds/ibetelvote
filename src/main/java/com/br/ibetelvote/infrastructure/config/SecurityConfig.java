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
                .csrf(AbstractHttpConfigurer::disable) // Desabilita CSRF para API stateless
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configura CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Configura para ser stateless (JWT)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/validate").permitAll()

                        // Documentação e ferramentas de desenvolvimento
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/h2-console/**").permitAll() // Permite acesso ao H2 console em dev
                        .requestMatchers("/favicon.ico").permitAll()

                        // Actuator
                        .requestMatchers("/actuator/**").permitAll()

                        // Arquivos estáticos
                        .requestMatchers(HttpMethod.GET, "/files/**").permitAll()

                        // Endpoints de autenticação autorizados
                        .requestMatchers("/api/v1/auth/me", "/api/v1/auth/logout").authenticated()

                        // Membros - acesso baseado em roles
                        .requestMatchers(HttpMethod.GET, "/api/v1/membros").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/membros").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/membros/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/membros/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/membros/*/foto").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // Eleições - acesso baseado em roles
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/eleicoes/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/ativar").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/encerrar").hasRole("ADMINISTRADOR")

                        // Cargos
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/cargos").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/cargos").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cargos/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/cargos/**").hasRole("ADMINISTRADOR")

                        // Candidatos
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/candidatos").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/candidatos").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/candidatos/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/candidatos/**").hasRole("ADMINISTRADOR")

                        // Votação
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/votacao").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/eleicoes/*/votar").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/progresso").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // Resultados
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/*/resultados").hasAnyRole("MEMBRO", "UTILIZADOR_PRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/eleicoes/historico").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // Upload de arquivos
                        .requestMatchers(HttpMethod.POST, "/api/v1/upload/**").hasAnyRole("UTILIZADOR_PRO", "ADMINISTRADOR")

                        // Qualquer outra requisição deve ser autenticada
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())) // Necessário para o H2 console
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
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

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
     * O custo de 12 é um bom balanço entre segurança e performance.
     * @return Uma instância de PasswordEncoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}