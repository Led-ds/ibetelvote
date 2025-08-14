package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.application.auth.dto.LoginResponse;

import java.util.UUID;

public interface AutoCadastroService {

    ValidarMembroResponse validarMembro(ValidarMembroRequest request);

    LoginResponse createUserByMembro(ValidarMembroRequest dadosMembro, CreateUserByMembroRequest dadosUsuario);

    MembroProfileResponse getMembroProfile(UUID userId);

    MembroProfileResponse updateMembroProfile(UUID userId, UpdateMembroProfileRequest request);

    boolean canMembroCreateUser(String email, String cpf);

}