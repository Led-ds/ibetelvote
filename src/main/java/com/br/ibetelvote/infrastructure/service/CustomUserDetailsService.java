package com.br.ibetelvote.infrastructure.service;

import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.infrastructure.auth.CustomUserDetails;
import com.br.ibetelvote.infrastructure.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Carregando usuário por email: {}", username);

        User user = userJpaRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", username);
                    return new UsernameNotFoundException("Usuário não encontrado: " + username);
                });

        // ✅ ADICIONE ESTE LOG TEMPORÁRIO
        log.info("DEBUG - Usuário encontrado: {} | Hash: {} | Ativo: {}",
                user.getEmail(),
                user.getPassword().substring(0, 10) + "...",
                user.isEnabled());

        return user;
    }
}