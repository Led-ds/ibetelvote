package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoResponse;
import com.br.ibetelvote.application.cargo.dto.CreateCargoRequest;
import com.br.ibetelvote.application.cargo.dto.UpdateCargoRequest;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.br.ibetelvote.domain.services.CargoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cargos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cargos", description = "Endpoints para gestão de cargos ministeriais da igreja")
@SecurityRequirement(name = "bearerAuth")
public class CargoController {

    private final CargoService cargoService;

    // === OPERAÇÕES BÁSICAS ===

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar cargo", description = "Cria um novo cargo ministerial no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cargo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Cargo já existe com este nome")
    })
    public ResponseEntity<CargoResponse> createCargo(@Valid @RequestBody CreateCargoRequest request) {
        CargoResponse response = cargoService.createCargo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar cargo por ID", description = "Retorna os dados de um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<CargoResponse> getCargoById(@PathVariable UUID id) {
        CargoResponse response = cargoService.getCargoById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Listar cargos", description = "Lista todos os cargos com paginação e filtros")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Page<CargoResponse>> getAllCargos(
            @Parameter(description = "Nome do cargo") @RequestParam(required = false) String nome,
            @Parameter(description = "ID da categoria") @RequestParam(required = false) UUID categoriaId,
            @Parameter(description = "Hierarquia do cargo") @RequestParam(required = false) HierarquiaCargo hierarquia,
            @Parameter(description = "Status ativo") @RequestParam(required = false) Boolean ativo,
            @Parameter(description = "Disponível para eleições") @RequestParam(required = false) Boolean disponivelEleicao,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {

        Page<CargoResponse> response = cargoService.getAllCargosComFiltros(
                nome, categoriaId, hierarquia, ativo, disponivelEleicao, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Listar todos os cargos", description = "Lista todos os cargos sem paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> getAllCargosList() {
        List<CargoResponse> response = cargoService.getAllCargos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar cargo", description = "Atualiza os dados de um cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Nome já existe")
    })
    public ResponseEntity<CargoResponse> updateCargo(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCargoRequest request) {
        CargoResponse response = cargoService.updateCargo(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover cargo", description = "Remove um cargo do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cargo removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Cargo não pode ser removido"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<Void> deleteCargo(@PathVariable UUID id) {
        cargoService.deleteCargo(id);
        return ResponseEntity.noContent().build();
    }

    // === CONSULTAS POR CATEGORIA ===

    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos por categoria", description = "Lista cargos de uma categoria específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<List<CargoResponse>> getCargosByCategoria(@PathVariable UUID categoriaId) {
        List<CargoResponse> cargos = cargoService.getCargosByCategoria(categoriaId);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/categoria/{categoriaId}/ordenados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos ordenados por categoria", description = "Lista cargos ordenados por precedência")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    public ResponseEntity<List<CargoResponse>> getCargosOrdenadosByCategoria(@PathVariable UUID categoriaId) {
        List<CargoResponse> cargos = cargoService.getCargosOrdenadosByCategoria(categoriaId);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/categoria/{categoriaId}/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos ativos por categoria", description = "Lista cargos ativos de uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosAtivosByCategoria(@PathVariable UUID categoriaId) {
        List<CargoResponse> cargos = cargoService.getCargosAtivosByCategoria(categoriaId);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/categoria/{categoriaId}/disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos disponíveis por categoria", description = "Lista cargos disponíveis para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosDisponiveisByCategoria(@PathVariable UUID categoriaId) {
        List<CargoResponse> cargos = cargoService.getCargosDisponiveisByCategoria(categoriaId);
        return ResponseEntity.ok(cargos);
    }

    // === CONSULTAS POR HIERARQUIA ===

    @GetMapping("/hierarquia/{hierarquia}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos por hierarquia", description = "Lista cargos de uma hierarquia específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosByHierarquia(@PathVariable HierarquiaCargo hierarquia) {
        List<CargoResponse> cargos = cargoService.getCargosByHierarquia(hierarquia);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/hierarquia/{hierarquia}/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos ativos por hierarquia", description = "Lista cargos ativos de uma hierarquia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosAtivosByHierarquia(@PathVariable HierarquiaCargo hierarquia) {
        List<CargoResponse> cargos = cargoService.getCargosAtivosByHierarquia(hierarquia);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/ministeriais")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos ministeriais", description = "Lista cargos ministeriais (Pastoral, Presbiteral, Diaconal)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosMinisteriais() {
        List<CargoResponse> cargos = cargoService.getCargosMinisteriais();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/ministeriais/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos ministeriais ativos", description = "Lista cargos ministeriais ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosMinisteriaisAtivos() {
        List<CargoResponse> cargos = cargoService.getCargosMinisteriaisAtivos();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/lideranca")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos de liderança", description = "Lista cargos de liderança")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosLideranca() {
        List<CargoResponse> cargos = cargoService.getCargosLideranca();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/administrativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos administrativos", description = "Lista cargos administrativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosAdministrativos() {
        List<CargoResponse> cargos = cargoService.getCargosAdministrativos();
        return ResponseEntity.ok(cargos);
    }

    // === CONSULTAS ESPECÍFICAS ===

    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos ativos", description = "Lista todos os cargos ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosAtivos() {
        List<CargoResponse> cargos = cargoService.getCargosAtivos();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos disponíveis", description = "Lista cargos disponíveis para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosDisponiveis() {
        List<CargoResponse> cargos = cargoService.getCargosDisponiveis();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/disponiveis/page")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos disponíveis paginados", description = "Lista cargos disponíveis com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Page<CargoResponse>> getCargosDisponiveis(
            @PageableDefault(size = 20, sort = "categoria.ordemExibicao,ordemPrecedencia,nome") Pageable pageable) {
        Page<CargoResponse> cargos = cargoService.getCargosDisponiveis(pageable);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/inativos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cargos inativos", description = "Lista todos os cargos inativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosInativos() {
        List<CargoResponse> cargos = cargoService.getCargosInativos();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/incompletos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Cargos incompletos", description = "Lista cargos com informações incompletas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosIncompletos() {
        List<CargoResponse> cargos = cargoService.getCargosIncompletos();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/com-candidatos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos com candidatos", description = "Lista cargos que possuem candidatos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosComCandidatos() {
        List<CargoResponse> cargos = cargoService.getCargosComCandidatos();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar por nome", description = "Busca cargos por nome (busca parcial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> searchCargosByNome(
            @Parameter(description = "Nome para busca") @RequestParam String nome) {
        List<CargoResponse> cargos = cargoService.getCargosByNome(nome);
        return ResponseEntity.ok(cargos);
    }

    // === OPERAÇÕES DE STATUS ===

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar cargo", description = "Ativa um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<CargoResponse> ativarCargo(@PathVariable UUID id) {
        CargoResponse response = cargoService.ativarCargo(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar cargo", description = "Desativa um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo desativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<CargoResponse> desativarCargo(@PathVariable UUID id) {
        CargoResponse response = cargoService.desativarCargo(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/ativar-para-eleicao")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar para eleições", description = "Disponibiliza cargo para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo disponibilizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado"),
            @ApiResponse(responseCode = "400", description = "Cargo não pode ser disponibilizado")
    })
    public ResponseEntity<CargoResponse> ativarParaEleicao(@PathVariable UUID id) {
        CargoResponse response = cargoService.ativarParaEleicao(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desativar-para-eleicao")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar para eleições", description = "Remove cargo das eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo removido das eleições com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<CargoResponse> desativarParaEleicao(@PathVariable UUID id) {
        CargoResponse response = cargoService.desativarParaEleicao(id);
        return ResponseEntity.ok(response);
    }

    // === OPERAÇÕES DE PRECEDÊNCIA ===

    @PatchMapping("/{id}/alterar-ordem")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Alterar ordem de precedência", description = "Altera a ordem de precedência de um cargo na categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem alterada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Ordem já em uso")
    })
    public ResponseEntity<CargoResponse> alterarOrdemPrecedencia(
            @PathVariable UUID id,
            @Parameter(description = "Nova ordem de precedência") @RequestParam Integer novaOrdem) {
        CargoResponse response = cargoService.alterarOrdemPrecedencia(id, novaOrdem);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categoria/{categoriaId}/proxima-ordem")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Próxima ordem na categoria", description = "Retorna próxima ordem de precedência disponível")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ordem retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Integer> getProximaOrdemPrecedencia(@PathVariable UUID categoriaId) {
        Integer ordem = cargoService.getProximaOrdemPrecedencia(categoriaId);
        return ResponseEntity.ok(ordem);
    }

    @PostMapping("/categoria/{categoriaId}/reorganizar-ordens")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Reorganizar ordens da categoria", description = "Reorganiza ordens de precedência na categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reorganização realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> reorganizarOrdensCategoria(@PathVariable UUID categoriaId) {
        List<CargoResponse> cargos = cargoService.reorganizarOrdensCategoria(categoriaId);
        return ResponseEntity.ok(cargos);
    }

    // === OPERAÇÕES DE ELEGIBILIDADE ===

    @GetMapping("/elegiveis-para/{cargoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos elegíveis", description = "Lista cargos que podem se candidatar ao cargo especificado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosElegiveisParaCargo(@PathVariable UUID cargoId) {
        List<CargoResponse> cargos = cargoService.getCargosElegiveisParaCargo(cargoId);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/elegiveis-para-hierarquia/{hierarquia}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos elegíveis para hierarquia", description = "Lista cargos elegíveis para uma hierarquia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosElegiveisParaHierarquia(@PathVariable HierarquiaCargo hierarquia) {
        List<CargoResponse> cargos = cargoService.getCargosElegiveisParaHierarquia(hierarquia);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/elegiveis-para-membro/{nivelMembro}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos elegíveis para membro", description = "Lista cargos que um membro pode se candidatar")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosElegiveisParaMembro(@PathVariable String nivelMembro) {
        List<CargoResponse> cargos = cargoService.getCargosElegiveisParaMembro(nivelMembro);
        return ResponseEntity.ok(cargos);
    }

    @PostMapping("/verificar-elegibilidade")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Verificar elegibilidade", description = "Verifica se um cargo pode candidatar-se a outro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Boolean> verificarElegibilidade(
            @Parameter(description = "Cargo origem") @RequestParam UUID cargoOrigemId,
            @Parameter(description = "Cargo destino") @RequestParam UUID cargoDestinoId) {
        boolean elegivel = cargoService.verificarElegibilidade(cargoOrigemId, cargoDestinoId);
        return ResponseEntity.ok(elegivel);
    }

    // === VALIDAÇÕES ===

    @GetMapping("/exists/nome")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se nome existe", description = "Verifica se cargo com nome específico já existe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> existsCargoByNome(
            @Parameter(description = "Nome do cargo") @RequestParam String nome) {
        boolean exists = cargoService.existsCargoByNome(nome);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/disponivel/nome")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar disponibilidade do nome", description = "Verifica se nome está disponível")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> isNomeDisponivel(
            @Parameter(description = "Nome do cargo") @RequestParam String nome) {
        boolean disponivel = cargoService.isNomeDisponivel(nome);
        return ResponseEntity.ok(disponivel);
    }

    @GetMapping("/{id}/can-delete")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se pode deletar", description = "Verifica se um cargo pode ser removido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
    })
    public ResponseEntity<Boolean> canDeleteCargo(@PathVariable UUID id) {
        boolean canDelete = cargoService.canDeleteCargo(id);
        return ResponseEntity.ok(canDelete);
    }

    @GetMapping("/categoria/{categoriaId}/ordem/{ordem}/disponivel")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar ordem disponível", description = "Verifica se ordem está disponível na categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> isOrdemPrecedenciaDisponivel(
            @PathVariable UUID categoriaId,
            @PathVariable Integer ordem) {
        boolean disponivel = cargoService.isOrdemPrecedenciaDisponivel(categoriaId, ordem);
        return ResponseEntity.ok(disponivel);
    }

    // === ESTATÍSTICAS ===

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de cargos", description = "Retorna o total de cargos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Long> getTotalCargos() {
        long total = cargoService.getTotalCargos();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de cargos ativos", description = "Retorna o total de cargos ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Long> getTotalCargosAtivos() {
        long total = cargoService.getTotalCargosAtivos();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de cargos disponíveis", description = "Retorna total de cargos disponíveis para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Long> getTotalCargosDisponiveis() {
        long total = cargoService.getTotalCargosDisponiveis();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/por-categoria")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas por categoria", description = "Retorna estatísticas de cargos por categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, Object>> getEstatisticasPorCategoria() {
        Map<String, Object> stats = cargoService.getEstatisticasPorCategoria();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/por-hierarquia")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas por hierarquia", description = "Retorna estatísticas de cargos por hierarquia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, Object>> getEstatisticasPorHierarquia() {
        Map<String, Object> stats = cargoService.getEstatisticasPorHierarquia();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/gerais")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Estatísticas gerais", description = "Retorna estatísticas gerais dos cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, Object>> getEstatisticasGerais() {
        Map<String, Object> stats = cargoService.getEstatisticasGerais();
        return ResponseEntity.ok(stats);
    }

    // === RELATÓRIOS ===

    @GetMapping("/relatorios/completo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Relatório completo", description = "Gera relatório completo dos cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<Map<String, Object>>> getRelatorioCompleto() {
        List<Map<String, Object>> relatorio = cargoService.getRelatorioCompleto();
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/relatorios/hierarquia-por-categoria")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Relatório hierarquia por categoria", description = "Relatório detalhado por categoria e hierarquia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Map<String, List<CargoResponse>>> getRelatorioHierarquiaPorCategoria() {
        Map<String, List<CargoResponse>> relatorio = cargoService.getRelatorioHierarquiaPorCategoria();
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/relatorios/recentes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos recentes", description = "Lista cargos criados recentemente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosRecentes() {
        List<CargoResponse> cargos = cargoService.getCargosRecentes();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/relatorios/por-periodo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos por período", description = "Lista cargos criados em período específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosPorPeriodo(
            @Parameter(description = "Data início") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Data fim") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<CargoResponse> cargos = cargoService.getCargosPorPeriodo(inicio, fim);
        return ResponseEntity.ok(cargos);
    }

    // === UTILITÁRIOS ===

    @GetMapping("/basic-info")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Informações básicas", description = "Retorna informações básicas dos cargos ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoBasicInfo>> getCargosBasicInfo() {
        List<CargoBasicInfo> basicInfo = cargoService.getCargosBasicInfo();
        return ResponseEntity.ok(basicInfo);
    }

    @GetMapping("/categoria/{categoriaId}/basic-info")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Informações básicas por categoria", description = "Informações básicas dos cargos de uma categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<CargoBasicInfo>> getCargosBasicInfoByCategoria(@PathVariable UUID categoriaId) {
        List<CargoBasicInfo> basicInfo = cargoService.getCargosBasicInfoByCategoria(categoriaId);
        return ResponseEntity.ok(basicInfo);
    }

    @GetMapping("/hierarquias-disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Hierarquias disponíveis", description = "Lista todas as hierarquias disponíveis")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<Map<String, Object>>> getHierarquiasDisponiveis() {
        List<Map<String, Object>> hierarquias = cargoService.getHierarquiasDisponiveis();
        return ResponseEntity.ok(hierarquias);
    }

    @GetMapping("/niveis-elegibilidade")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Níveis de elegibilidade", description = "Lista todos os níveis de elegibilidade disponíveis")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<Map<String, Object>>> getNiveisElegibilidade() {
        List<Map<String, Object>> niveis = cargoService.getNiveisElegibilidade();
        return ResponseEntity.ok(niveis);
    }

    // === OPERAÇÕES EM LOTE ===

    @PatchMapping("/ativar-multiplos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar múltiplos", description = "Ativa múltiplos cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargos ativados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> ativarCargos(@RequestBody List<UUID> ids) {
        List<CargoResponse> cargos = cargoService.ativarCargos(ids);
        return ResponseEntity.ok(cargos);
    }

    @PatchMapping("/desativar-multiplos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar múltiplos", description = "Desativa múltiplos cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargos desativados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> desativarCargos(@RequestBody List<UUID> ids) {
        List<CargoResponse> cargos = cargoService.desativarCargos(ids);
        return ResponseEntity.ok(cargos);
    }

    @DeleteMapping("/deletar-multiplos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Deletar múltiplos", description = "Remove múltiplos cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cargos removidos com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "400", description = "Algum cargo não pode ser removido")
    })
    public ResponseEntity<Void> deleteCargos(@RequestBody List<UUID> ids) {
        cargoService.deleteCargos(ids);
        return ResponseEntity.noContent().build();
    }

    // === EXCEPTION HANDLERS ===

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_STATE")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Erro interno no controller de cargos", e);
        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Erro interno do servidor")
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ErrorResponse DTO interno
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String code;
        private String message;
        private String path;
        private java.time.LocalDateTime timestamp;
    }
}
