package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.auth.dto.LoginResponse;
import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.application.mapper.AuthMapper;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.entities.enus.UserRole;
import com.br.ibetelvote.domain.services.AutoCadastroService;
import com.br.ibetelvote.infrastructure.jwt.JwtService;
import com.br.ibetelvote.infrastructure.repositories.MembroJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AutoCadastroServiceImpl implements AutoCadastroService {

    private final MembroJpaRepository membroRepository;
    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Override
    @Transactional(readOnly = true)
    public ValidarMembroResponse validarMembro(ValidarMembroRequest request) {
        log.info("Validando membro para auto-cadastro: email={}, cpf={}", request.getEmail(), request.getCpf());

        // Validar CPF
        if (!Membro.isValidCPF(request.getCpf())) {
            throw new IllegalArgumentException("CPF inválido");
        }

        // Buscar membro por email e CPF
        Membro membro = membroRepository.findByEmailAndCpf(request.getEmail(), request.getCpf())
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado com os dados informados"));

        // Verificar se pode criar usuário
        if (!membro.canCreateUser()) {
            String motivo = !membro.isActive() ? "Membro não está ativo" : "Membro já possui usuário cadastrado";
            throw new IllegalArgumentException(motivo);
        }

        return ValidarMembroResponse.builder()
                .membroId(membro.getId())
                .nome(membro.getNome())
                .email(membro.getEmail())
                .cpf(membro.getCpf())
                .podeCreateUser(true)
                .message("Dados validados com sucesso! Você pode criar seu usuário.")
                .build();
    }

    @Override
    public LoginResponse createUserByMembro(ValidarMembroRequest dadosMembro, CreateUserByMembroRequest dadosUsuario) {
        log.info("Criando usuário para membro: email={}", dadosMembro.getEmail());

        // Re-validar membro
        ValidarMembroResponse validacao = validarMembro(dadosMembro);

        // Validar senhas
        if (!dadosUsuario.isPasswordsMatch()) {
            throw new IllegalArgumentException("Senhas não conferem");
        }

        // Buscar membro
        Membro membro = membroRepository.findById(validacao.getMembroId())
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));

        // Verificar se email não está em uso por outro usuário
        if (userRepository.existsByEmail(membro.getEmail())) {
            throw new IllegalArgumentException("Email já está sendo usado por outro usuário");
        }

        // Criar usuário
        User novoUser = User.builder()
                .email(membro.getEmail())
                .password(passwordEncoder.encode(dadosUsuario.getPassword()))
                .role(UserRole.MEMBRO)
                .ativo(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        User savedUser = userRepository.save(novoUser);

        // Associar usuário ao membro
        membro.associateUser(savedUser.getId());
        membroRepository.save(membro);

        // Gerar tokens para login automático
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        log.info("Usuário criado e associado com sucesso - Membro: {}, User: {}", membro.getId(), savedUser.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMinutes() * 60)
                .user(authMapper.toUserProfileResponse(savedUser))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MembroProfileResponse getMembroProfile(UUID membroId) {
        log.debug("Buscando perfil do membro: {}", membroId);

        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));

        return MembroProfileResponse.builder()
                .id(membro.getId())
                .nome(membro.getNome())
                .email(membro.getEmail())
                .cpf(membro.getCpf())
                .dataNascimento(membro.getDataNascimento())
                .cargo(membro.getCargo())
                .departamento(membro.getDepartamento())
                .dataBatismo(membro.getDataBatismo())
                .dataMembroDesde(membro.getDataMembroDesde())
                .telefone(membro.getTelefone())
                .celular(membro.getCelular())
                .endereco(membro.getEndereco())
                .cidade(membro.getCidade())
                .estado(membro.getEstado())
                .cep(membro.getCep())
                .foto(membro.getFoto())
                .observacoes(membro.getObservacoes())
                .ativo(membro.getAtivo())
                .hasUser(membro.hasUser())
                .isBasicProfileComplete(membro.isBasicProfileComplete())
                .isFullProfileComplete(membro.isFullProfileComplete())
                .photoUrl(membro.getPhotoUrl())
                .createdAt(membro.getCreatedAt())
                .updatedAt(membro.getUpdatedAt())
                .build();
    }

    @Override
    public MembroProfileResponse updateMembroProfile(UUID membroId, UpdateMembroProfileRequest request) {
        log.info("Atualizando perfil do membro: {}", membroId);

        Membro membro = membroRepository.findById(membroId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));

        // Verificar se email não está sendo usado por outro membro
        if (!membro.getEmail().equals(request.getEmail()) &&
                membroRepository.existsByEmailAndIdNot(request.getEmail(), membroId)) {
            throw new IllegalArgumentException("Email já está sendo usado por outro membro");
        }

        // Atualizar dados básicos
        membro.updateBasicProfile(
                request.getNome(),
                request.getEmail(),
                request.getDataNascimento(),
                membro.getCpf() // CPF não pode ser alterado
        );

        // Atualizar dados completos
        membro.updateCompleteProfile(
                request.getTelefone(),
                request.getCelular(),
                request.getEndereco(),
                request.getCidade(),
                request.getEstado(),
                request.getCep(),
                request.getCargo(),
                request.getDepartamento(),
                request.getDataBatismo(),
                request.getDataMembroDesde()
        );

        // Atualizar observações
        membro.updateObservations(request.getObservacoes());

        Membro savedMembro = membroRepository.save(membro);

        log.info("Perfil atualizado com sucesso - Membro: {}", membroId);

        return getMembroProfile(savedMembro.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canMembroCreateUser(String email, String cpf) {
        return membroRepository.findByEmailAndCpf(email, cpf)
                .map(Membro::canCreateUser)
                .orElse(false);
    }
}