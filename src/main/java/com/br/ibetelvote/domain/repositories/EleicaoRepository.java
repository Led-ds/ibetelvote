package com.br.ibetelvote.domain.repositories;

import com.br.ibetelvote.domain.entities.Eleicao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EleicaoRepository {

    // === OPERAÇÕES BÁSICAS ===
    void deleteById(UUID id);
    long count();

    // === CONSULTAS COM CANDIDATOS ===
    Optional<Eleicao> findByIdWithCandidatos(UUID id);
    Optional<Eleicao> findByIdWithCandidatosAndVotos(UUID id);
    List<Eleicao> findAllWithCandidatosAprovados();

    // === CONSULTAS POR STATUS ===
    List<Eleicao> findByAtivaTrue();
    List<Eleicao> findByAtivaFalse();
    Optional<Eleicao> findEleicaoAtiva();
    Optional<Eleicao> findEleicaoAtivaComCandidatos();

    // === CONSULTAS POR DATA E PERÍODO ===
    List<Eleicao> findEleicoesAbertas();
    List<Eleicao> findEleicoesEncerradas();
    List<Eleicao> findEleicoesFuturas();
    List<Eleicao> findByDataInicioBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Eleicao> findEleicoesAtivasNoPeriodo(LocalDateTime inicio, LocalDateTime fim);

    // === CONSULTAS ESPECÍFICAS ===
    Page<Eleicao> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    List<Eleicao> findRecentEleicoes(int limit);
    List<Eleicao> findEleicoesComCandidatosAprovados();

    // === VALIDAÇÕES DE NEGÓCIO ===
    boolean existeEleicaoAtivaNoMesmoPeriodo(LocalDateTime inicio, LocalDateTime fim, UUID excludeId);
    boolean existeEleicaoAtiva();

    // === ESTATÍSTICAS ===
    long countByAtivaTrue();
    long countEleicoesEncerradas();
    long countEleicoesFuturas();
    long countEleicoesComCandidatos();
    int countCargosComCandidatosNaEleicao(UUID eleicaoId);
    int countCandidatosAprovadosNaEleicao(UUID eleicaoId);

    // === CONSULTAS PARA RELATÓRIOS ===
    List<Eleicao> findEleicoesParaRelatorio(LocalDateTime inicio, LocalDateTime fim);
    List<Eleicao> findEleicoesComMaiorParticipacao(int limit);
}