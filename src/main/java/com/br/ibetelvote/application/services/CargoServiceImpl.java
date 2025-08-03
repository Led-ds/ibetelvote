package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.eleicao.dto.*;
import com.br.ibetelvote.application.mapper.CargoMapper;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.services.CargoService;
import com.br.ibetelvote.infrastructure.repositories.CargoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.EleicaoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CargoServiceImpl implements CargoService {

    private final CargoJpaRepository cargoRepository;
    private final EleicaoJpaRepository eleicaoRepository;
    private final CargoMapper cargoMapper;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = {"cargos", "eleicoes"}, allEntries = true)
    public CargoResponse createCargo(CreateCargoRequest request) {
        log.info("Criando novo cargo: {} para eleição: {}", request.getNome(), request.getEleicaoId());

        // Validar se eleição existe
        Eleicao eleicao = eleicaoRepository.findById(request.getEleicaoId())
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + request.getEleicaoId()));

        // Validar se eleição pode receber novos cargos
        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível adicionar cargos a eleição com votação em andamento");
        }

        // Validar se nome já existe na eleição
        if (cargoRepository.existsByNomeAndEleicaoId(request.getNome(), request.getEleicaoId())) {
            throw new IllegalArgumentException("Já existe um cargo com este nome nesta eleição");
        }

        Cargo cargo = cargoMapper.toEntity(request);

        // Definir ordem de votação se não foi especificada
        if (cargo.getOrdemVotacao() == null) {
            long totalCargos = cargoRepository.countByEleicaoId(request.getEleicaoId());
            cargo.setOrdemVotacao((int) (totalCargos + 1));
        }

        Cargo savedCargo = cargoRepository.save(cargo);

        log.info("Cargo criado com sucesso - ID: {}, Nome: {}", savedCargo.getId(), savedCargo.getNome());
        return cargoMapper.toResponse(savedCargo);
    }

    @Override
    @Cacheable(value = "cargos", key = "#id")
    @Transactional(readOnly = true)
    public CargoResponse getCargoById(UUID id) {
        log.debug("Buscando cargo por ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        return cargoMapper.toResponse(cargo);
    }

    @Override
    @Cacheable(value = "cargos-eleicao", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosByEleicaoId(UUID eleicaoId) {
        log.debug("Buscando cargos da eleição: {}", eleicaoId);

        List<Cargo> cargos = cargoRepository.findByEleicaoId(eleicaoId);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @CacheEvict(value = {"cargos", "eleicoes"}, allEntries = true)
    public CargoResponse updateCargo(UUID id, UpdateCargoRequest request) {
        log.info("Atualizando cargo ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        // Validar se pode ser atualizado
        if (cargo.getEleicao() != null && cargo.getEleicao().isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível atualizar cargo de eleição com votação em andamento");
        }

        // Validar nome único se foi alterado
        if (request.getNome() != null && !request.getNome().equals(cargo.getNome())) {
            if (cargoRepository.existsByNomeAndEleicaoId(request.getNome(), cargo.getEleicaoId())) {
                throw new IllegalArgumentException("Já existe um cargo com este nome nesta eleição");
            }
        }

        cargoMapper.updateEntityFromRequest(request, cargo);
        Cargo updatedCargo = cargoRepository.save(cargo);

        log.info("Cargo atualizado com sucesso - ID: {}", updatedCargo.getId());
        return cargoMapper.toResponse(updatedCargo);
    }

    @Override
    @CacheEvict(value = {"cargos", "eleicoes"}, allEntries = true)
    public void deleteCargo(UUID id) {
        log.info("Removendo cargo ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        // Validar se pode ser removido
        if (!canDeleteCargo(id)) {
            throw new IllegalStateException("Não é possível remover cargo que possui candidatos ou votos");
        }

        cargoRepository.delete(cargo);
        log.info("Cargo removido com sucesso - ID: {}", id);
    }

    // === OPERAÇÕES ESPECÍFICAS ===

    @Override
    @Cacheable(value = "cargos-ordenados", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosByEleicaoIdOrdenados(UUID eleicaoId) {
        log.debug("Buscando cargos ordenados da eleição: {}", eleicaoId);

        List<Cargo> cargos = cargoRepository.findByEleicaoIdOrderByOrdemVotacao(eleicaoId);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @CacheEvict(value = {"cargos", "cargos-ordenados"}, allEntries = true)
    public void reordernarCargos(UUID eleicaoId, List<UUID> cargoIds) {
        log.info("Reordenando cargos da eleição: {}", eleicaoId);

        // Validar se eleição existe
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada com ID: " + eleicaoId));

        // Validar se pode reordenar
        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível reordenar cargos durante votação");
        }

        // Atualizar ordem dos cargos
        for (int i = 0; i < cargoIds.size(); i++) {
            UUID cargoId = cargoIds.get(i);
            Cargo cargo = cargoRepository.findById(cargoId)
                    .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + cargoId));

            if (!cargo.getEleicaoId().equals(eleicaoId)) {
                throw new IllegalArgumentException("Cargo não pertence à eleição especificada");
            }

            cargo.setOrdemVotacao(i + 1);
            cargoRepository.save(cargo);
        }

        log.info("Cargos reordenados com sucesso para eleição: {}", eleicaoId);
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean existsCargoByNomeAndEleicao(String nome, UUID eleicaoId) {
        return cargoRepository.existsByNomeAndEleicaoId(nome, eleicaoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCargo(UUID id) {
        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        // Não pode deletar se tem candidatos ou votos
        return cargo.getCandidatos().isEmpty() && cargo.getVotos().isEmpty();
    }

    // === ESTATÍSTICAS ===

    @Override
    @Cacheable(value = "cargo-stats", key = "#eleicaoId")
    @Transactional(readOnly = true)
    public long getTotalCargosByEleicao(UUID eleicaoId) {
        return cargoRepository.countByEleicaoId(eleicaoId);
    }

    @Override
    @Cacheable(value = "cargos-obrigatorios")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosObrigatorios() {
        List<Cargo> cargos = cargoRepository.findByObrigatorioTrue();
        return cargoMapper.toResponseList(cargos);
    }
}