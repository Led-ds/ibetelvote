package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoResponse;
import com.br.ibetelvote.application.cargo.dto.CreateCargoRequest;
import com.br.ibetelvote.application.cargo.dto.UpdateCargoRequest;
import com.br.ibetelvote.application.mapper.CargoMapper;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.enums.ElegibilidadeCargo;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.br.ibetelvote.domain.services.CargoService;
import com.br.ibetelvote.infrastructure.repositories.CargoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.CategoriaJpaRepository;
import com.br.ibetelvote.infrastructure.specifications.CargoSpecifications;
import org.springframework.data.jpa.domain.Specification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CargoServiceImpl implements CargoService {

    private final CargoJpaRepository cargoRepository;
    private final CategoriaJpaRepository categoriaRepository;
    private final CargoMapper cargoMapper;

    // === OPERAÇÕES BÁSICAS ===

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse createCargo(CreateCargoRequest request) {
        log.info("Criando novo cargo: {}", request.getNome());

        // Validar dados
        validarDadosCargo(request.getNome(), request.getDescricao(), request.getCategoriaId(),
                request.getHierarquia(), request.getElegibilidade());

        // Verificar se nome já existe
        if (existsCargoByNome(request.getNome())) {
            throw new IllegalArgumentException("Já existe um cargo com o nome: " + request.getNome());
        }

        // Verificar se categoria existe
        if (!categoriaRepository.existsById(request.getCategoriaId())) {
            throw new IllegalArgumentException("Categoria não encontrada com ID: " + request.getCategoriaId());
        }

        // Definir próxima ordem se não foi informada
        if (request.getOrdemPrecedencia() == null) {
            request.setOrdemPrecedencia(getProximaOrdemPrecedencia(request.getCategoriaId()));
        } else {
            // Verificar se ordem está disponível
            if (!isOrdemPrecedenciaDisponivel(request.getCategoriaId(), request.getOrdemPrecedencia())) {
                throw new IllegalArgumentException("Ordem de precedência " + request.getOrdemPrecedencia() +
                        " já está em uso na categoria");
            }
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
            if (!isNomeDisponivelParaAtualizacao(request.getNome(), id)) {
                throw new IllegalArgumentException("Já existe um cargo com o nome: " + request.getNome());
            }
        }

        // Validar categoria se foi alterada
        if (request.getCategoriaId() != null && !Objects.equals(request.getCategoriaId(), cargo.getCategoriaId())) {
            if (!categoriaRepository.existsById(request.getCategoriaId())) {
                throw new IllegalArgumentException("Categoria não encontrada com ID: " + request.getCategoriaId());
            }
        }

        // Validar ordem de precedência se foi alterada
        if (request.getOrdemPrecedencia() != null) {
            UUID categoriaId = request.getCategoriaId() != null ? request.getCategoriaId() : cargo.getCategoriaId();
            if (!cargoRepository.isOrdemPrecedenciaDisponivel(categoriaId, request.getOrdemPrecedencia(), id)) {
                throw new IllegalArgumentException("Ordem de precedência " + request.getOrdemPrecedencia() +
                        " já está em uso na categoria");
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
            throw new IllegalStateException("Cargo não pode ser removido pois está sendo usado em eleições");
        }

        cargoRepository.delete(cargo);
        log.info("Cargo removido com sucesso - ID: {}", id);
    }

    // === CONSULTAS COM FILTROS ===

    @Override
    @Transactional(readOnly = true)
    public Page<CargoResponse> getAllCargosComFiltros(String nome, UUID categoriaId, HierarquiaCargo hierarquia,
                                                      Boolean ativo, Boolean disponivelEleicao, Pageable pageable) {
        log.debug("Buscando cargos com filtros: nome={}, categoria={}, hierarquia={}, ativo={}, disponivel={}",
                nome, categoriaId, hierarquia, ativo, disponivelEleicao);

        Specification<Cargo> spec = CargoSpecifications.comNome(nome)
                .and(CargoSpecifications.daCategoria(categoriaId))
                .and(CargoSpecifications.comHierarquia(hierarquia))
                .and(CargoSpecifications.ativo(ativo))
                .and(CargoSpecifications.disponivelParaEleicao(disponivelEleicao))
                .and(CargoSpecifications.ordenadoPorPrecedencia());

        Page<Cargo> cargos = cargoRepository.findAll(spec, pageable);
        return cargos.map(cargoMapper::toResponse);
    }

    // === CONSULTAS POR CATEGORIA ===

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosByCategoria(UUID categoriaId) {
        log.debug("Buscando cargos da categoria: {}", categoriaId);

        List<Cargo> cargos = cargoRepository.findByCategoria_Id(categoriaId);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosOrdenadosByCategoria(UUID categoriaId) {
        log.debug("Buscando cargos ordenados da categoria: {}", categoriaId);

        List<Cargo> cargos = cargoRepository.findByCategoria_IdOrderByOrdemPrecedenciaAscNomeAsc(categoriaId);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosAtivosByCategoria(UUID categoriaId) {
        log.debug("Buscando cargos ativos da categoria: {}", categoriaId);

        List<Cargo> cargos = cargoRepository.findByCategoria_IdAndAtivoTrue(categoriaId);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosDisponiveisByCategoria(UUID categoriaId) {
        log.debug("Buscando cargos disponíveis da categoria: {}", categoriaId);

        List<Cargo> cargos = cargoRepository.findCargosDisponiveisByCategoriaId(categoriaId);
        return cargoMapper.toResponseList(cargos);
    }

    // === CONSULTAS POR HIERARQUIA ===

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosByHierarquia(HierarquiaCargo hierarquia) {
        log.debug("Buscando cargos da hierarquia: {}", hierarquia);

        List<Cargo> cargos = cargoRepository.findByHierarquia(hierarquia);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosAtivosByHierarquia(HierarquiaCargo hierarquia) {
        log.debug("Buscando cargos ativos da hierarquia: {}", hierarquia);

        List<Cargo> cargos = cargoRepository.findByHierarquiaAndAtivoTrue(hierarquia);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Cacheable(value = "cargos-ministeriais")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosMinisteriais() {
        log.debug("Buscando cargos ministeriais");

        List<Cargo> cargos = cargoRepository.findCargosMinisteriais();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Cacheable(value = "cargos-ministeriais-ativos")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosMinisteriaisAtivos() {
        log.debug("Buscando cargos ministeriais ativos");

        List<Cargo> cargos = cargoRepository.findCargosMinisteriaisAtivos();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Cacheable(value = "cargos-lideranca")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosLideranca() {
        log.debug("Buscando cargos de liderança");

        List<Cargo> cargos = cargoRepository.findCargosLideranca();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosAdministrativos() {
        log.debug("Buscando cargos administrativos");

        List<Cargo> cargos = cargoRepository.findCargosAdministrativos();
        return cargoMapper.toResponseList(cargos);
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
    @Cacheable(value = "cargos-disponiveis")
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosDisponiveis() {
        log.debug("Buscando cargos disponíveis para eleições");

        List<Cargo> cargos = cargoRepository.findCargosDisponiveis();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Cacheable(value = "cargos-disponiveis-page")
    @Transactional(readOnly = true)
    public Page<CargoResponse> getCargosDisponiveis(Pageable pageable) {
        log.debug("Buscando cargos disponíveis com paginação");

        Page<Cargo> cargos = cargoRepository.findCargosDisponiveis(pageable);
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
    public List<CargoResponse> getCargosIncompletos() {
        log.debug("Buscando cargos com informações incompletas");

        Specification<Cargo> spec = CargoSpecifications.comInformacoesIncompletas();
        List<Cargo> cargos = cargoRepository.findAll(spec);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosComCandidatos() {
        log.debug("Buscando cargos que possuem candidatos");

        Specification<Cargo> spec = CargoSpecifications.comCandidatos()
                .and(CargoSpecifications.ordenadoPorNome());

        List<Cargo> cargos = cargoRepository.findAll(spec);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosByNome(String nome) {
        log.debug("Buscando cargos por nome: {}", nome);

        List<Cargo> cargos = cargoRepository.findByNomeContainingIgnoreCase(nome);
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
        // Quando desativa o cargo, também remove das eleições
        cargo.desativarParaEleicao();

        Cargo savedCargo = cargoRepository.save(cargo);

        log.info("Cargo desativado com sucesso - ID: {}", savedCargo.getId());
        return cargoMapper.toResponse(savedCargo);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse ativarParaEleicao(UUID id) {
        log.info("Ativando cargo para eleições ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        // Verificar se pode ser disponibilizado para eleições
        if (!cargo.isAtivo()) {
            throw new IllegalStateException("Não é possível disponibilizar para eleições um cargo inativo");
        }

        if (!cargo.temInformacoesCompletas()) {
            throw new IllegalStateException("Cargo não possui informações suficientes para ser disponibilizado para eleições");
        }

        cargo.ativarParaEleicao();
        Cargo savedCargo = cargoRepository.save(cargo);

        log.info("Cargo disponibilizado para eleições - ID: {}", savedCargo.getId());
        return cargoMapper.toResponse(savedCargo);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse desativarParaEleicao(UUID id) {
        log.info("Desativando cargo para eleições ID: {}", id);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        cargo.desativarParaEleicao();
        Cargo savedCargo = cargoRepository.save(cargo);

        log.info("Cargo removido das eleições - ID: {}", savedCargo.getId());
        return cargoMapper.toResponse(savedCargo);
    }

    // === OPERAÇÕES DE PRECEDÊNCIA ===

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public CargoResponse alterarOrdemPrecedencia(UUID id, Integer novaOrdem) {
        log.info("Alterando ordem de precedência do cargo ID: {} para ordem: {}", id, novaOrdem);

        Cargo cargo = cargoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + id));

        // Verificar se nova ordem está disponível
        if (!cargoRepository.isOrdemPrecedenciaDisponivel(cargo.getCategoriaId(), novaOrdem, id)) {
            throw new IllegalArgumentException("Ordem de precedência " + novaOrdem + " já está em uso na categoria");
        }

        cargo.setOrdemPrecedencia(novaOrdem);
        Cargo savedCargo = cargoRepository.save(cargo);

        return cargoMapper.toResponse(savedCargo);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getProximaOrdemPrecedencia(UUID categoriaId) {
        return cargoRepository.findNextOrdemPrecedenciaByCategoriaId(categoriaId);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public List<CargoResponse> reorganizarOrdensCategoria(UUID categoriaId) {
        log.info("Reorganizando ordens de precedência na categoria: {}", categoriaId);

        List<Cargo> cargos = cargoRepository.findCargosParaReorganizacao(categoriaId);

        for (int i = 0; i < cargos.size(); i++) {
            Cargo cargo = cargos.get(i);
            cargo.setOrdemPrecedencia(i + 1);
            cargoRepository.save(cargo);
        }

        log.info("Reorganização concluída para {} cargos na categoria {}", cargos.size(), categoriaId);
        return cargoMapper.toResponseList(cargos);
    }

    // === OPERAÇÕES DE ELEGIBILIDADE ===

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosElegiveisParaCargo(UUID cargoId) {
        log.debug("Buscando cargos elegíveis para o cargo: {}", cargoId);

        // Primeiro busca o cargo para pegar o nome
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado com ID: " + cargoId));

        Specification<Cargo> spec = CargoSpecifications.podeElegerPara(cargo.getNome())
                .and(CargoSpecifications.ativo(true))
                .and(CargoSpecifications.ordenadoPorNome());

        List<Cargo> cargos = cargoRepository.findAll(spec);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosElegiveisParaHierarquia(HierarquiaCargo hierarquia) {
        log.debug("Buscando cargos elegíveis para hierarquia: {}", hierarquia);

        Specification<Cargo> spec = CargoSpecifications.podeElegerParaHierarquia(hierarquia)
                .and(CargoSpecifications.ativo(true))
                .and(CargoSpecifications.ordenadoPorHierarquia());

        List<Cargo> cargos = cargoRepository.findAll(spec);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosElegiveisParaMembro(String nivelMembro) {
        log.debug("Buscando cargos elegíveis para membro nível: {}", nivelMembro);

        Specification<Cargo> spec = CargoSpecifications.elegiveisParaNivelMembro(nivelMembro)
                .and(CargoSpecifications.ativo(true))
                .and(CargoSpecifications.disponivelParaEleicao(true))
                .and(CargoSpecifications.ordenadoPorPrecedencia());

        List<Cargo> cargos = cargoRepository.findAll(spec);
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarElegibilidade(UUID cargoOrigemId, UUID cargoDestinoId) {
        log.debug("Verificando elegibilidade entre cargos: origem={}, destino={}", cargoOrigemId, cargoDestinoId);

        Specification<Cargo> spec = CargoSpecifications.elegibilidadeEntreCargos(cargoOrigemId, cargoDestinoId);

        return cargoRepository.findOne(spec).isPresent();
    }

    // === VALIDAÇÕES ===

    @Override
    @Transactional(readOnly = true)
    public boolean existsCargoByNome(String nome) {
        Specification<Cargo> spec = CargoSpecifications.comNomeExato(nome);
        return cargoRepository.findOne(spec).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNomeDisponivel(String nome) {
        return !existsCargoByNome(nome);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNomeDisponivelParaAtualizacao(String nome, UUID cargoId) {
        Specification<Cargo> spec = CargoSpecifications.comNomeExatoExcluindoId(nome, cargoId);
        return cargoRepository.findOne(spec).isEmpty();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCargo(UUID id) {
        return cargoRepository.canDeleteCargo(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOrdemPrecedenciaDisponivel(UUID categoriaId, Integer ordem) {
        Specification<Cargo> spec = CargoSpecifications.comOrdemPrecedencia(categoriaId, ordem);
        return cargoRepository.findOne(spec).isEmpty();
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

    @Override
    @Cacheable(value = "cargo-stats-categoria")
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticasPorCategoria() {
        Map<String, Object> stats = new HashMap<>();
        List<Object[]> dadosCategoria = cargoRepository.countCargosPorCategoria();

        Map<String, Long> categoriaStats = dadosCategoria.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0], // nome categoria
                        row -> ((Number) row[1]).longValue() // count
                ));

        stats.put("cargosPorCategoria", categoriaStats);
        stats.put("totalCategorias", categoriaStats.size());
        stats.put("categoriaMaisUtilizada", categoriaStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A"));

        return stats;
    }

    @Override
    @Cacheable(value = "cargo-stats-hierarquia")
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticasPorHierarquia() {
        Map<String, Object> stats = new HashMap<>();
        List<Object[]> dadosHierarquia = cargoRepository.countCargosPorHierarquia();

        Map<String, Long> hierarquiaStats = dadosHierarquia.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(), // hierarquia
                        row -> ((Number) row[1]).longValue() // count
                ));

        stats.put("cargosPorHierarquia", hierarquiaStats);
        stats.put("totalHierarquias", hierarquiaStats.size());
        stats.put("hierarquiaMaisUtilizada", hierarquiaStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A"));

        return stats;
    }

    @Override
    @Cacheable(value = "cargo-stats-gerais")
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticasGerais() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", getTotalCargos());
        stats.put("ativos", getTotalCargosAtivos());
        stats.put("inativos", getTotalCargosInativos());
        stats.put("disponiveis", getTotalCargosDisponiveis());
        stats.put("percentualAtivos", calculatePercentual(getTotalCargosAtivos(), getTotalCargos()));
        stats.put("percentualDisponiveis", calculatePercentual(getTotalCargosDisponiveis(), getTotalCargos()));

        // Estatísticas por hierarquia
        long ministeriais = cargoRepository.findCargosMinisteriais().size();
        long lideranca = cargoRepository.findCargosLideranca().size();
        long administrativos = cargoRepository.findCargosAdministrativos().size();

        stats.put("ministeriais", ministeriais);
        stats.put("lideranca", lideranca);
        stats.put("administrativos", administrativos);

        return stats;
    }

    // === RELATÓRIOS ===

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRelatorioCompleto() {
        log.debug("Gerando relatório completo dos cargos");

        List<Cargo> cargos = cargoRepository.findAll();

        return cargos.stream().map(cargo -> {
            Map<String, Object> relatorio = new HashMap<>();
            relatorio.put("id", cargo.getId());
            relatorio.put("nome", cargo.getNome());
            relatorio.put("descricao", cargo.getDescricao());
            relatorio.put("categoria", cargo.getCategoriaNome());
            relatorio.put("hierarquia", cargo.getHierarquiaDisplayName());
            relatorio.put("ordemPrecedencia", cargo.getOrdemPrecedencia());
            relatorio.put("ativo", cargo.isAtivo());
            relatorio.put("disponivelEleicao", cargo.isDisponivelEleicao());
            relatorio.put("temInformacoesCompletas", cargo.temInformacoesCompletas());
            relatorio.put("elegibilidade", cargo.getElegibilidadeFormatada());
            relatorio.put("criadoEm", cargo.getCreatedAt());
            relatorio.put("atualizadoEm", cargo.getUpdatedAt());
            return relatorio;
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<CargoResponse>> getRelatorioHierarquiaPorCategoria() {
        log.debug("Gerando relatório de hierarquia por categoria");

        List<Cargo> todosCargos = cargoRepository.findAll();

        return todosCargos.stream()
                .collect(Collectors.groupingBy(
                        Cargo::getCategoriaNome,
                        Collectors.mapping(
                                cargoMapper::toResponse,
                                Collectors.toList()
                        )
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosRecentes() {
        log.debug("Buscando cargos criados recentemente");

        List<Cargo> cargos = cargoRepository.findTop10ByOrderByCreatedAtDesc();
        return cargoMapper.toResponseList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoResponse> getCargosPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        log.debug("Buscando cargos por período: {} - {}", inicio, fim);

        Specification<Cargo> spec = CargoSpecifications.criadosEntre(inicio, fim)
                .and(CargoSpecifications.ordenadoPorNome());

        List<Cargo> cargos = cargoRepository.findAll(spec);
        return cargoMapper.toResponseList(cargos);
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
    @Transactional(readOnly = true)
    public List<CargoBasicInfo> getCargosBasicInfoByCategoria(UUID categoriaId) {
        log.debug("Buscando informações básicas dos cargos da categoria: {}", categoriaId);

        List<Cargo> cargos = cargoRepository.findByCategoria_IdAndAtivoTrue(categoriaId);
        return cargoMapper.toBasicInfoList(cargos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getHierarquiasDisponiveis() {
        log.debug("Buscando hierarquias disponíveis");

        return Arrays.stream(HierarquiaCargo.values())
                .map(hierarquia -> {
                    Map<String, Object> hierarquiaInfo = new HashMap<>();
                    hierarquiaInfo.put("codigo", hierarquia.name());
                    hierarquiaInfo.put("nome", hierarquia.getDisplayName());
                    hierarquiaInfo.put("descricao", hierarquia.getDescricao());
                    hierarquiaInfo.put("ordem", hierarquia.getOrdem());
                    hierarquiaInfo.put("cor", hierarquia.getCor());
                    hierarquiaInfo.put("icone", hierarquia.getIcone());
                    return hierarquiaInfo;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getNiveisElegibilidade() {
        log.debug("Buscando níveis de elegibilidade disponíveis");

        return Arrays.stream(ElegibilidadeCargo.values())
                .map(nivel -> {
                    Map<String, Object> nivelInfo = new HashMap<>();
                    nivelInfo.put("codigo", nivel.name());
                    nivelInfo.put("nome", nivel.getDisplayName());
                    nivelInfo.put("descricao", nivel.getDescricao());
                    nivelInfo.put("nivel", nivel.getNivel());
                    nivelInfo.put("cor", nivel.getCor());
                    nivelInfo.put("icone", nivel.getIcone());
                    return nivelInfo;
                })
                .toList();
    }

    // === OPERAÇÕES EM LOTE ===

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public List<CargoResponse> ativarCargos(List<UUID> ids) {
        log.info("Ativando múltiplos cargos: {}", ids.size());

        List<Cargo> cargos = cargoRepository.findAllById(ids);

        cargos.forEach(Cargo::activate);
        List<Cargo> savedCargos = cargoRepository.saveAll(cargos);

        return cargoMapper.toResponseList(savedCargos);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public List<CargoResponse> desativarCargos(List<UUID> ids) {
        log.info("Desativando múltiplos cargos: {}", ids.size());

        List<Cargo> cargos = cargoRepository.findAllById(ids);

        cargos.forEach(cargo -> {
            cargo.deactivate();
            cargo.desativarParaEleicao(); // Remove também das eleições
        });
        List<Cargo> savedCargos = cargoRepository.saveAll(cargos);

        return cargoMapper.toResponseList(savedCargos);
    }

    @Override
    @CacheEvict(value = "cargos", allEntries = true)
    public void deleteCargos(List<UUID> ids) {
        log.info("Removendo múltiplos cargos: {}", ids.size());

        // Validar cada cargo individualmente
        for (UUID id : ids) {
            if (!canDeleteCargo(id)) {
                throw new IllegalStateException("Cargo com ID " + id + " não pode ser removido pois está sendo usado");
            }
        }

        cargoRepository.deleteAllById(ids);
        log.info("Cargos removidos com sucesso: {}", ids.size());
    }

    // === VALIDAÇÃO DE DADOS ===

    @Override
    public void validarDadosCargo(String nome, String descricao, UUID categoriaId,
                                  HierarquiaCargo hierarquia, List<String> elegibilidade) {
        if (!Cargo.isNomeValido(nome)) {
            throw new IllegalArgumentException("Nome do cargo inválido. Deve ter entre 3 e 100 caracteres.");
        }

        if (descricao != null && descricao.length() > 1000) {
            throw new IllegalArgumentException("Descrição do cargo deve ter no máximo 1000 caracteres.");
        }

        if (categoriaId == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }

        if (hierarquia == null) {
            throw new IllegalArgumentException("Hierarquia é obrigatória.");
        }

        if (!Cargo.isElegibilidadeValida(elegibilidade)) {
            throw new IllegalArgumentException("Lista de elegibilidade inválida.");
        }

        // Validar se elegibilidade é compatível com hierarquia
        if (elegibilidade != null && !elegibilidade.isEmpty()) {
            boolean compativel = switch (hierarquia) {
                case PASTORAL -> elegibilidade.contains("PRESBITERO") || elegibilidade.contains("PASTOR");
                case PRESBITERAL -> elegibilidade.contains("DIACONO") || elegibilidade.contains("PRESBITERO");
                case DIACONAL -> elegibilidade.contains("MEMBRO") || elegibilidade.contains("OBREIRO");
                case AUXILIAR -> elegibilidade.contains("MEMBRO") || elegibilidade.contains("OBREIRO");
                case LIDERANCA, ADMINISTRATIVO -> true; // Aceita qualquer elegibilidade
            };

            if (!compativel) {
                throw new IllegalArgumentException("Elegibilidade não é compatível com a hierarquia " + hierarquia.getDisplayName());
            }
        }
    }

    // === MÉTODOS AUXILIARES ===

    private double calculatePercentual(long valor, long total) {
        if (total == 0) return 0.0;
        return (double) valor / total * 100;
    }
}