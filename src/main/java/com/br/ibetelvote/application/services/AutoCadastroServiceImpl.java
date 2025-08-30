package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.auth.dto.LoginResponse;
import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.application.mapper.AuthMapper;
import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.entities.User;
import com.br.ibetelvote.domain.entities.enums.UserRole;
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
    public MembroProfileResponse getMembroProfile(UUID userId) {
        log.debug("Buscando perfil do membro: {}", userId);

        Membro membro = membroRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));

        return MembroProfileResponse.builder()
                .id(membro.getId())
                .nome(membro.getNome())
                .email(membro.getEmail())
                .cpf(membro.getCpf())
                .dataNascimento(membro.getDataNascimento())

                .cargoAtualId(membro.getCargoAtualId())           // UUID do cargo atual
                .nomeCargoAtual(membro.getNomeCargoAtual())       // Nome do cargo para exibição

                .departamento(membro.getDepartamento())
                .dataBatismo(membro.getDataBatismo())
                .dataMembroDesde(membro.getDataMembroDesde())
                .telefone(membro.getTelefone())
                .celular(membro.getCelular())
                .endereco(membro.getEndereco())
                .cidade(membro.getCidade())
                .estado(membro.getEstado())
                .cep(membro.getCep())
                .fotoBase64(membro.getFotoBase64())
                .fotoTipo(membro.getFotoTipo())
                .fotoNome(membro.getFotoNome())
                .temFoto(membro.hasPhoto())
                .observacoes(membro.getObservacoes())
                .ativo(membro.getAtivo())
                .hasUser(membro.hasUser())

                .isBasicProfileComplete(isBasicProfileComplete(membro))
                .isFullProfileComplete(isFullProfileComplete(membro))

                .createdAt(membro.getCreatedAt())
                .updatedAt(membro.getUpdatedAt())
                .build();
    }

    @Override
    public MembroProfileResponse updateMembroProfile(UUID userId, UpdateMembroProfileRequest request) {
        log.info("Atualizando perfil do membro: {}", userId);

        Membro membro = membroRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado"));

        if (!membro.getEmail().equals(request.getEmail()) &&
                membroRepository.existsByEmailAndIdNot(request.getEmail(), membro.getId())) {
            throw new IllegalArgumentException("Email já está sendo usado por outro membro");
        }

        // Atualizar dados básicos
        membro.updateBasicProfile(
                request.getNome(),
                request.getEmail(),
                request.getDataNascimento(),
                membro.getCpf() // CPF não pode ser alterado
        );

        // Atualizar dados de contato
        if (request.getTelefone() != null) {
            membro.setTelefone(request.getTelefone());
        }
        if (request.getCelular() != null) {
            membro.setCelular(request.getCelular());
        }

        // Atualizar endereço
        if (request.getEndereco() != null) {
            membro.setEndereco(request.getEndereco());
        }
        if (request.getCidade() != null) {
            membro.setCidade(request.getCidade());
        }
        if (request.getEstado() != null) {
            membro.setEstado(request.getEstado());
        }
        if (request.getCep() != null) {
            membro.setCep(request.getCep());
        }

        // Atualizar cargo (se existir no DTO)
        if (request.getCargoAtualId() != null) {
            membro.setCargoAtualId(request.getCargoAtualId());
        }

        // Atualizar dados da igreja
        if (request.getDepartamento() != null) {
            membro.setDepartamento(request.getDepartamento());
        }
        if (request.getDataBatismo() != null) {
            membro.setDataBatismo(request.getDataBatismo());
        }
        if (request.getDataMembroDesde() != null) {
            membro.setDataMembroDesde(request.getDataMembroDesde());
        }

        // Atualizar observações
        if (request.getObservacoes() != null) {
            membro.setObservacoes(request.getObservacoes());
        }

        Membro savedMembro = membroRepository.save(membro);

        log.info("Perfil atualizado com sucesso - Membro: {}", membro.getId());

        return getMembroProfile(savedMembro.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canMembroCreateUser(String email, String cpf) {
        return membroRepository.findByEmailAndCpf(email, cpf)
                .map(Membro::canCreateUser)
                .orElse(false);
    }

    private boolean isBasicProfileComplete(Membro membro) {
        return membro.getNome() != null && !membro.getNome().trim().isEmpty() &&
                membro.getEmail() != null && !membro.getEmail().trim().isEmpty() &&
                membro.getCpf() != null && !membro.getCpf().trim().isEmpty() &&
                membro.getTelefone() != null && !membro.getTelefone().trim().isEmpty();
    }

    private boolean isFullProfileComplete(Membro membro) {
        return isBasicProfileComplete(membro) &&
                membro.getDataNascimento() != null &&
                membro.getEndereco() != null && !membro.getEndereco().trim().isEmpty() &&
                membro.getCidade() != null && !membro.getCidade().trim().isEmpty() &&
                membro.getEstado() != null && !membro.getEstado().trim().isEmpty() &&
                membro.getCep() != null && !membro.getCep().trim().isEmpty();
    }

}