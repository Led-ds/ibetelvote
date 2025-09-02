package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Membro;
import com.br.ibetelvote.domain.repositories.MembroRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembroJpaRepository extends JpaRepository<Membro, UUID>, MembroRepository {

    // === OPERAÇÕES BÁSICAS ===
    // Métodos básicos herdados do JpaRepository

    // === CONSULTAS POR STATUS ===
    List<Membro> findByAtivoTrue();
    List<Membro> findByAtivoFalse();
    Page<Membro> findByAtivo(Boolean ativo, Pageable pageable);
    long countByAtivo(Boolean ativo);

    // === CONSULTAS POR DADOS PESSOAIS ===
    @Query("SELECT m FROM Membro m WHERE m.email = :email AND m.cpf = :cpf")
    Optional<Membro> findByEmailAndCpf(@Param("email") String email, @Param("cpf") String cpf);

    @Query("SELECT m FROM Membro m WHERE UPPER(m.email) = UPPER(:email)")
    Optional<Membro> findByEmail(@Param("email") String email);

    Optional<Membro> findByCpf(String cpf);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Membro m WHERE UPPER(m.email) = UPPER(:email)")
    boolean existsByEmail(@Param("email") String email);

    boolean existsByCpf(String cpf);

    @Query("SELECT m FROM Membro m WHERE UPPER(m.nome) LIKE UPPER(CONCAT('%', :nome, '%'))")
    List<Membro> findByNomeContainingIgnoreCase(@Param("nome") String nome);


    @Query("SELECT COUNT(m) FROM Membro m WHERE m.ativo = true")
    Long countMembrosAtivos();

    @Query("SELECT COUNT(m) FROM Membro m WHERE m.ativo = true AND m.user IS NOT NULL")
    Long countMembrosElegiveisParaVotacao();


    // === CONSULTAS POR CARGO ===
    List<Membro> findByCargoAtualId(UUID cargoId);

    @Query("SELECT m FROM Membro m WHERE m.cargoAtualId IS NULL")
    List<Membro> findByCargoAtualIdIsNull();

    @Query("SELECT m FROM Membro m WHERE m.cargoAtualId IS NOT NULL")
    List<Membro> findByCargoAtualIdIsNotNull();

    Page<Membro> findByCargoAtualId(UUID cargoId, Pageable pageable);

    long countByCargoAtualId(UUID cargoId);

    // === CONSULTAS POR USER ===
    Optional<Membro> findByUserId(UUID userId);

    @Query("SELECT m FROM Membro m WHERE m.userId IS NULL")
    List<Membro> findByUserIdIsNull();

    @Query("SELECT m FROM Membro m WHERE m.userId IS NOT NULL")
    List<Membro> findByUserIdIsNotNull();

    boolean existsByUserId(UUID userId);

    // === CONSULTAS ORDENADAS ===
    List<Membro> findAllByOrderByNomeAsc();
    List<Membro> findByAtivoTrueOrderByNomeAsc();
    Page<Membro> findByAtivoTrueOrderByNomeAsc(Pageable pageable);

    // === CONSULTAS PARA VALIDAÇÃO ===
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Membro m WHERE UPPER(m.email) = UPPER(:email) AND m.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Membro m WHERE m.cpf = :cpf AND m.id != :id")
    boolean existsByCpfAndIdNot(@Param("cpf") String cpf, @Param("id") UUID id);

    // === CONSULTAS CUSTOMIZADAS ===

    /**
     * Busca membros aptos para votação (ativos com informações completas)
     */
    @Query("SELECT m FROM Membro m WHERE m.ativo = true AND m.nome IS NOT NULL AND m.email IS NOT NULL AND m.cpf IS NOT NULL ORDER BY m.nome")
    List<Membro> findMembrosAptosParaVotacao();

    /**
     * Busca membros elegíveis para candidatura a um cargo específico
     * Baseado na hierarquia: Obreiro->Diácono, Diácono->Diácono/Presbítero, Presbítero->Presbítero
     */
    @Query("SELECT m FROM Membro m JOIN m.cargoAtual c WHERE m.ativo = true AND " +
            "(:nomeCargo = 'Diácono' AND (c.nome = 'Obreiro' OR c.nome = 'Diácono')) OR " +
            "(:nomeCargo = 'Presbítero' AND (c.nome = 'Diácono' OR c.nome = 'Presbítero')) " +
            "ORDER BY m.nome")
    List<Membro> findMembrosElegiveisParaCargo(@Param("nomeCargo") String nomeCargo);

    /**
     * Conta membros por cargo
     */
    @Query("SELECT COUNT(m) FROM Membro m WHERE m.cargoAtualId = :cargoId AND m.ativo = true")
    long countMembrosPorCargo(@Param("cargoId") UUID cargoId);

    /**
     * Busca membros sem cargo definido
     */
    @Query("SELECT m FROM Membro m WHERE m.cargoAtualId IS NULL AND m.ativo = true ORDER BY m.nome")
    List<Membro> findMembrosSemCargo();

    /**
     * Busca membros com perfil completo
     */
    @Query("SELECT m FROM Membro m WHERE m.ativo = true AND m.nome IS NOT NULL AND m.email IS NOT NULL AND " +
            "m.cpf IS NOT NULL AND m.dataNascimento IS NOT NULL AND " +
            "(m.telefone IS NOT NULL OR m.celular IS NOT NULL) ORDER BY m.nome")
    List<Membro> findMembrosComPerfilCompleto();

    // === CONSULTAS ESTATÍSTICAS ===

    /**
     * Conta membros ativos por departamento
     */
    @Query("SELECT m.departamento, COUNT(m) FROM Membro m WHERE m.ativo = true AND m.departamento IS NOT NULL GROUP BY m.departamento")
    List<Object[]> countMembrosPorDepartamento();

    /**
     * Busca membros que podem criar usuário
     */
    @Query("SELECT m FROM Membro m WHERE m.ativo = true AND m.cpf IS NOT NULL AND m.email IS NOT NULL AND m.userId IS NULL ORDER BY m.nome")
    List<Membro> findMembrosQuePodemCriarUsuario();

    /**
     * Busca membros por período de cadastro
     */
    @Query("SELECT m FROM Membro m WHERE m.dataMembroDesde >= :dataInicio AND m.dataMembroDesde <= :dataFim ORDER BY m.dataMembroDesde DESC")
    List<Membro> findMembrosPorPeriodoIngresso(@Param("dataInicio") java.time.LocalDate dataInicio,
                                               @Param("dataFim") java.time.LocalDate dataFim);

    // === CONSULTAS PARA RELATÓRIOS ===

    /**
     * Busca membros recentes (últimos cadastrados)
     */
    @Query("SELECT m FROM Membro m WHERE m.ativo = true ORDER BY m.createdAt DESC")
    List<Membro> findTop10ByOrderByCreatedAtDesc();

    /**
     * Conta total de membros por status
     */
    @Query("SELECT m.ativo, COUNT(m) FROM Membro m GROUP BY m.ativo")
    List<Object[]> countMembrosPorStatus();
}