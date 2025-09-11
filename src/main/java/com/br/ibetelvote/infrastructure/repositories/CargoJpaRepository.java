package com.br.ibetelvote.infrastructure.repositories;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.br.ibetelvote.domain.repositories.CargoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository para Cargo - Versão Simplificada.
 * Implementa apenas os métodos essenciais da interface de domínio.
 * Métodos CRUD básicos são fornecidos automaticamente pelo JpaRepository.
 * Complexidade migrada para CargoSpecifications + CargoService.
 */
@Repository
public interface CargoJpaRepository extends JpaRepository<Cargo, UUID>,
        JpaSpecificationExecutor<Cargo>,
        CargoRepository {

    // === IMPLEMENTAÇÃO DOS MÉTODOS DA INTERFACE DOMAIN ===
    @Override
    Optional<Cargo> findByNome(String nome);

    @Override
    boolean existsByNome(String nome);

    @Override
    List<Cargo> findByAtivoTrue();

    @Override
    List<Cargo> findByDisponivelEleicaoTrue();

    @Override
    List<Cargo> findByHierarquia(HierarquiaCargo hierarquia);

    @Override
    List<Cargo> findAllByOrderByNomeAsc();

    @Override
    List<Cargo> findByAtivoTrueOrderByNomeAsc();

    @Override
    boolean existsByNomeAndIdNot(String nome, UUID id);

    @Override
    @Query("SELECT CASE WHEN COUNT(cand) = 0 THEN true ELSE false END " +
            "FROM Candidato cand WHERE cand.cargoPretendido.id = :cargoId")
    boolean canDeleteCargo(@Param("cargoId") UUID cargoId);

    @Override
    @Query("SELECT c.nome FROM Cargo c WHERE c.id = :cargoId")
    Optional<String> findNomeById(@Param("cargoId") UUID cargoId);

    @Override
    @Query("SELECT c.hierarquia FROM Cargo c WHERE c.id = :cargoId")
    Optional<HierarquiaCargo> findHierarquiaById(@Param("cargoId") UUID cargoId);

}