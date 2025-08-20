package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoResponse;
import com.br.ibetelvote.application.cargo.dto.CreateCargoRequest;
import com.br.ibetelvote.application.cargo.dto.UpdateCargoRequest;
import com.br.ibetelvote.application.mapper.CargoMapper;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.services.CargoService;
import com.br.ibetelvote.infrastructure.repositories.CargoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CargoMapper cargoMapper;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse createCargo(CreateCargoRequest request) {
        log.info("Criando novo cargo: {}", request.getNome());

        // Validar dados
        validarDadosCargo(request.getNome(), request.getDescricao());

        // Verificar se nome já existe
        if (existsCargoByNome(request.getNome())) {
            throw new IllegalArgumentException("Já existe um cargo com o nome: " + request.getNome());
        }

        Cargo cargo = cargoMapper.toEntity(request);
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
    @Cacheable(value = "cargos-page")
    @Transactional(readOnly = true)
    public Page<CargoResponse> getAllCargos(Pageable pageable) {
        log.debug("Buscando todos os cargos com paginação");

        Page<Cargo> cargos = cargoRepository.findAll(pageable);
        return cargos.map(cargoMapper::toResponse);
    }

    @Override
    @Cacheable(value = "cargos-all")
    @Transactional(readOnly = true)
    public List<CargoResponse> getAllCargos() {
        log.debug("Buscando todos os cargos");

        List<Cargo> cargos = cargoRepository.findAllByOrderByNomeAsc();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse updateCargo(UUID id, UpdateCargoRequest request) {
        log.info("Atualizando cargo ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        // Validar nome se foi alterado
        if (request.getNome() != null && !request.getNome().equals(cargo.getNome())) {
            validarDadosCargo(request.getNome(), request.getDescricao());

            if (!isNomeDisponivelParaAtualizacao(request.getNome(), id)) {
                throw new IllegalArgumentException("Já existe um cargo com o nome: " + request.getNome());
            }
        }

        cargoMapper.updateEntityFromRequest(request, cargo);
        Cargo updatedCargo = cargoRepository.save(cargo);

        log.info("Cargo atualizado com sucesso - ID: {}", updatedCargo.getId());
        return cargoMapper.toResponse(updatedCargo);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public void deleteCargo(UUID id) {
        log.info("Removendo cargo ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        // Validar se pode ser removido
        if (!canDeleteCargo(id)) {
            throw new IllegalStateException("Cargo não pode ser removido pois está em uso");
        }

        cargoRepository.delete(cargo);
        log.info("Cargo removido com sucesso - ID: {}", id);
    }

    // === CONSULTAS ESPECÍFICAS ===

    @Override
    @Cacheable(value = "cargos-ativos")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosAtivos() {
        log.debug("Buscando cargos ativos");

        List<Cargo> cargos = cargoRepository.findByAtivoTrueOrderByNomeAsc();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Cacheable(value = "cargos-ativos-page")
    @Transactional(readOnly = true)
    public Page<CargoResponse> getCargosAtivos(Pageable pageable) {
        log.debug("Buscando cargos ativos com paginação");

        Page<Cargo> cargos = cargoRepository.findByAtivoTrueOrderByNomeAsc(pageable);
        return cargos.map(cargoMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosInativos() {
        log.debug("Buscando cargos inativos");

        List<Cargo> cargos = cargoRepository.findByAtivoFalse();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosByNome(String nome) {
        log.debug("Buscando cargos por nome: {}", nome);

        List<Cargo> cargos = cargoRepository.findByNomeContainingIgnoreCase(nome);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Cacheable(value = "cargos-disponiveis")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosDisponiveis() {
        log.debug("Buscando cargos disponíveis para eleições");

        List<Cargo> cargos = cargoRepository.findCargosDisponiveis();
        return cargoMapper.toResponseList(cargos);
    }

    // === OPERAÇÕES DE STATUS ===

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse ativarCargo(UUID id) {
        log.info("Ativando cargo ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        cargo.activate();
        Cargo savedCargo = cargoRepository.save(cargo);

        log.info("Cargo ativado com sucesso - ID: {}", savedCargo.getId());
        return cargoMapper.toResponse(savedCargo);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse desativarCargo(UUID id) {
        log.info("Desativando cargo ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        cargo.deactivate();
        Cargo savedCargo = cargoRepository.save(cargo);

        log.info("Cargo desativado com sucesso - ID: {}", savedCargo.getId());
        return cargoMapper.toResponse(savedCargo);
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean existsCargoByNome(String nome) {
        return cargoRepository.existsByNome(nome);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNomeDisponivel(String nome) {
        return !existsCargoByNome(nome);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNomeDisponivelParaAtualizacao(String nome, UUID cargoId) {
        return !cargoRepository.existsByNomeAndIdNot(nome, cargoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCargo(UUID id) {
        // Por enquanto, sempre pode deletar
        // Futuramente verificar se está sendo usado em candidaturas
        return cargoRepository.existsById(id);
    }

    // === ESTATÍSTICAS ===

    @Override
    @Cacheable(value = "cargo-stats-total")
    @Transactional(readOnly = true)
    public long getTotalCargos() {
        return cargoRepository.count();
    }

    @Override
    @Cacheable(value = "cargo-stats-ativos")
    @Transactional(readOnly = true)
    public long getTotalCargosAtivos() {
        return cargoRepository.countByAtivo(true);
    }

    @Override
    @Cacheable(value = "cargo-stats-inativos")
    @Transactional(readOnly = true)
    public long getTotalCargosInativos() {
        return cargoRepository.countByAtivo(false);
    }

    @Override
    @Cacheable(value = "cargo-stats-disponiveis")
    @Transactional(readOnly = true)
    public long getTotalCargosDisponiveis() {
        return cargoRepository.countCargosDisponiveis();
    }

    // === UTILITÁRIOS ===

    @Override
    @Cacheable(value = "cargos-basic-info")
    @Transactional(readOnly = true)
    public List<CargoBasicInfo> getCargosBasicInfo() {
        log.debug("Buscando informações básicas dos cargos ativos");

        List<Cargo> cargos = cargoRepository.findByAtivoTrueOrderByNomeAsc();
        return cargoMapper.toBasicInfoList(cargos);
    }

    @Override
    public void validarDadosCargo(String nome, String descricao) {
        if (!Cargo.isNomeValido(nome)) {
            throw new IllegalArgumentException("Nome do cargo inválido. Deve ter entre 3 e 100 caracteres.");
        }

        if (descricao != null && descricao.length() > 1000) {
            throw new IllegalArgumentException("Descrição do cargo deve ter no máximo 1000 caracteres.");
        }
    }
}