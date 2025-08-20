package com.br.ibetelvote.domain.services;

import com.br.ibetelvote.application.eleicao.dto.MembroBasicInfo;
import com.br.ibetelvote.application.membro.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MembroService {

    /**
     * Cria um novo membro
     */
    MembroResponse createMembro(CreateMembroRequest request);

    /**
     * Busca membro por ID
     */
    MembroResponse getMembroById(UUID id);

    /**
     * Lista todos os membros com paginação
     */
    Page<MembroResponse> getAllMembros(Pageable pageable);

    /**
     * Lista todos os membros
     */
    List<MembroResponse> getAllMembros();

    /**
     * Atualiza um membro existente
     */
    MembroResponse updateMembro(UUID id, UpdateMembroRequest request);

    /**
     * Remove um membro
     */
    void deleteMembro(UUID id);

    // === CONSULTAS ESPECÍFICAS ===

    /**
     * Lista membros ativos
     */
    List<MembroResponse> getMembrosAtivos();

    /**
     * Lista membros ativos com paginação
     */
    Page<MembroResponse> getMembrosAtivos(Pageable pageable);

    /**
     * Lista membros inativos
     */
    List<MembroResponse> getMembrosInativos();

    /**
     * Busca membros por nome (busca parcial)
     */
    List<MembroResponse> getMembrosByNome(String nome);

    /**
     * Busca membro por email
     */
    MembroResponse getMembroByEmail(String email);

    /**
     * Busca membro por CPF
     */
    MembroResponse getMembroByCpf(String cpf);

    /**
     * Lista membros por cargo
     */
    List<MembroResponse> getMembrosPorCargo(UUID cargoId);

    /**
     * Lista membros sem cargo definido
     */
    List<MembroResponse> getMembrosSemCargo();

    /**
     * Lista membros elegíveis para um cargo específico
     */
    List<MembroResponse> getMembrosElegiveisParaCargo(String nomeCargo);

    /**
     * Ativa um membro
     */
    MembroResponse ativarMembro(UUID id);

    /**
     * Desativa um membro
     */
    MembroResponse desativarMembro(UUID id);

    /**
     * Atualiza cargo de um membro
     */
    MembroResponse updateCargoMembro(UUID id, UpdateCargoMembroRequest request);

    /**
     * Remove cargo de um membro
     */
    MembroResponse removeCargoMembro(UUID id);

    /**
     * Faz upload da foto do membro
     */
    MembroResponse uploadFotoMembro(UUID id, MembroUploadFotoRequest request);

    /**
     * Remove foto do membro
     */
    MembroResponse removeFotoMembro(UUID id);

    /**
     * Busca foto do membro em Base64
     */
    String getFotoMembroBase64(UUID id);

    /**
     * Busca perfil completo do membro
     */
    MembroProfileResponse getMembroProfile(UUID id);

    /**
     * Atualiza perfil do membro (para uso do próprio membro)
     */
    MembroProfileResponse updateMembroProfile(UUID id, UpdateMembroProfileRequest request);

    /**
     * Valida dados do membro (email e CPF)
     */
    ValidarMembroResponse validarMembro(ValidarMembroRequest request);

    /**
     * Verifica elegibilidade do membro para eleições
     */
    MembroElegibilidadeResponse verificarElegibilidade(UUID id, List<String> cargosDisponiveis);

    /**
     * Lista membros aptos para votação
     */
    List<MembroResponse> getMembrosAptosParaVotacao();

    /**
     * Busca membros com filtros
     */
    Page<MembroListResponse> buscarMembrosComFiltros(MembroFilterRequest filtros, Pageable pageable);

    /**
     * Lista membros para listagem simples
     */
    List<MembroListResponse> getMembrosParaListagem();

    /**
     * Verifica se email está disponível
     */
    boolean isEmailDisponivel(String email);

    /**
     * Verifica se email está disponível para atualização
     */
    boolean isEmailDisponivelParaAtualizacao(String email, UUID membroId);

    /**
     * Verifica se CPF está disponível
     */
    boolean isCpfDisponivel(String cpf);

    /**
     * Verifica se CPF está disponível para atualização
     */
    boolean isCpfDisponivelParaAtualizacao(String cpf, UUID membroId);

    /**
     * Verifica se membro pode ser removido
     */
    boolean canDeleteMembro(UUID id);

    /**
     * Associa membro a um usuário
     */
    MembroResponse associarUsuario(UUID membroId, UUID userId);

    /**
     * Desassocia membro de usuário
     */
    MembroResponse desassociarUsuario(UUID membroId);

    /**
     * Lista membros que podem criar usuário
     */
    List<MembroResponse> getMembrosQuePodemCriarUsuario();

    /**
     * Conta total de membros
     */
    long getTotalMembros();

    /**
     * Conta membros ativos
     */
    long getTotalMembrosAtivos();

    /**
     * Conta membros inativos
     */
    long getTotalMembrosInativos();

    /**
     * Conta membros por cargo
     */
    long getTotalMembrosPorCargo(UUID cargoId);

    /**
     * Conta membros sem cargo
     */
    long getTotalMembrosSemCargo();

    /**
     * Conta membros aptos para votação
     */
    long getTotalMembrosAptosParaVotacao();

    /**
     * Retorna informações básicas de todos os membros ativos
     */
    List<MembroBasicInfo> getMembrosBasicInfo();

    /**
     * Valida dados básicos do membro
     */
    void validarDadosMembro(String nome, String email, String cpf);
}