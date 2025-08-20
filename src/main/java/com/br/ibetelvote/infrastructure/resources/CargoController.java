package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.cargo.dto.CargoBasicInfo;
import com.br.ibetelvote.application.cargo.dto.CargoResponse;
import com.br.ibetelvote.application.cargo.dto.CreateCargoRequest;
import com.br.ibetelvote.application.cargo.dto.UpdateCargoRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cargos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cargos", description = "Endpoints para gestão de cargos da igreja")
@SecurityRequirement(name = "bearerAuth")
public class CargoController {

    private final CargoService cargoService;

    // === OPERAÇÕES BÁSICAS ===

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar cargo", description = "Cria um novo cargo no sistema")
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
    @Operation(summary = "Listar cargos", description = "Lista todos os cargos com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Page<CargoResponse>> getAllCargos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<CargoResponse> response = cargoService.getAllCargos(pageable);
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

    @GetMapping("/ativos/page")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos ativos paginados", description = "Lista cargos ativos com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Page<CargoResponse>> getCargosAtivos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<CargoResponse> cargos = cargoService.getCargosAtivos(pageable);
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

    @GetMapping("/disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos disponíveis", description = "Lista cargos disponíveis para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosDisponiveis() {
        List<CargoResponse> cargos = cargoService.getCargosDisponiveis();
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar por nome", description = "Busca cargos por nome (busca parcial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
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

    // === VALIDAÇÕES ===

    @GetMapping("/exists/nome")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se nome existe", description = "Verifica se um cargo com nome específico já existe")
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

    // === ESTATÍSTICAS ===

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de cargos", description = "Retorna o total de cargos cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
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
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalCargosAtivos() {
        long total = cargoService.getTotalCargosAtivos();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/disponiveis")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de cargos disponíveis", description = "Retorna o total de cargos disponíveis para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalCargosDisponiveis() {
        long total = cargoService.getTotalCargosDisponiveis();
        return ResponseEntity.ok(total);
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