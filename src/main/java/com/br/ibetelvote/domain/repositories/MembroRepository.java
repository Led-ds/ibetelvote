package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Membro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembroRepository {

    // === OPERAÇÕES BÁSICAS ===
    void deleteById(UUID id);
    long count();

    // === CONSULTAS POR STATUS ===
    List<Membro> findByAtivoTrue();
    List<Membro> findByAtivoFalse();
    Page<Membro> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    // === CONSULTAS POR DADOS PESSOAIS ===
    Optional<Membro> findByEmail(String email);
    Optional<Membro> findByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    List<Membro> findByNomeContainingIgnoreCase(String nome);

    // === CONSULTAS POR CARGO ===
    List<Membro> findByCargoAtualId(UUID cargoId);
    List<Membro> findByCargoAtualIdIsNull();
    List<Membro> findByCargoAtualIdIsNotNull();
    Page<Membro> findByCargoAtualId(UUID cargoId, Pageable pageable);
    long countByCargoAtualId(UUID cargoId);

    // === CONSULTAS POR USER ===
    Optional<Membro> findByUserId(UUID userId);
    List<Membro> findByUserIdIsNull();
    List<Membro> findByUserIdIsNotNull();
    boolean existsByUserId(UUID userId);

    // === CONSULTAS ORDENADAS ===
    List<Membro> findAllByOrderByNomeAsc();
    List<Membro> findByAtivoTrueOrderByNomeAsc();
    Page<Membro> findByAtivoTrueOrderByNomeAsc(Pageable pageable);

    // === CONSULTAS PARA VALIDAÇÃO ===
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByCpfAndIdNot(String cpf, UUID id);

    // === CONSULTAS CUSTOMIZADAS ===
    /**
     * Busca membros aptos para votação (ativos com informações completas)
     */
    List<Membro> findMembrosAptosParaVotacao();

    /**
     * Busca membros elegíveis para candidatura a um cargo específico
     */
    List<Membro> findMembrosElegiveisParaCargo(String nomeCargo);

    /**
     * Conta membros por cargo
     */
    long countMembrosPorCargo(UUID cargoId);

    /**
     * Busca membros sem cargo definido
     */
    List<Membro> findMembrosSemCargo();

    /**
     * Busca membros com perfil completo
     */
    List<Membro> findMembrosComPerfilCompleto();
}