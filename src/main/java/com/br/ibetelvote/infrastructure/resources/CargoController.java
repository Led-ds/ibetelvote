package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.eleicao.dto.*;
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
@Tag(name = "Cargos", description = "Endpoints para gestão de cargos das eleições")
@SecurityRequirement(name = "bearerAuth")
public class CargoController {

    private final CargoService cargoService;

    // === OPERAÇÕES BÁSICAS ===

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar cargo", description = "Cria um novo cargo para uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cargo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Cargo já existe nesta eleição")
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar cargo", description = "Atualiza os dados de um cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Cargo não encontrado")
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

    // === CONSULTAS POR ELEIÇÃO ===

    @GetMapping("/eleicao/{eleicaoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos por eleição", description = "Lista todos os cargos de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<List<CargoResponse>> getCargosByEleicaoId(@PathVariable UUID eleicaoId) {
        List<CargoResponse> cargos = cargoService.getCargosByEleicaoId(eleicaoId);
        return ResponseEntity.ok(cargos);
    }

    @GetMapping("/eleicao/{eleicaoId}/ordenados")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Cargos ordenados por eleição", description = "Lista cargos de uma eleição ordenados por ordem de votação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<List<CargoResponse>> getCargosByEleicaoIdOrdenados(@PathVariable UUID eleicaoId) {
        List<CargoResponse> cargos = cargoService.getCargosByEleicaoIdOrdenados(eleicaoId);
        return ResponseEntity.ok(cargos);
    }

    // === OPERAÇÕES ESPECIAIS ===

    @PutMapping("/eleicao/{eleicaoId}/reordenar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Reordenar cargos", description = "Reordena a sequência de votação dos cargos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargos reordenados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Lista de cargos inválida"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Eleição não encontrada")
    })
    public ResponseEntity<Void> reordernarCargos(
            @PathVariable UUID eleicaoId,
            @RequestBody List<UUID> cargoIds) {
        cargoService.reordernarCargos(eleicaoId, cargoIds);
        return ResponseEntity.ok().build();
    }

    // === CONSULTAS ESPECIAIS ===

    @GetMapping("/obrigatorios")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Cargos obrigatórios", description = "Lista todos os cargos marcados como obrigatórios")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<CargoResponse>> getCargosObrigatorios() {
        List<CargoResponse> cargos = cargoService.getCargosObrigatorios();
        return ResponseEntity.ok(cargos);
    }

    // === VALIDAÇÕES ===

    @GetMapping("/exists")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se cargo existe", description = "Verifica se um cargo com nome específico já existe na eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> existsCargoByNomeAndEleicao(
            @Parameter(description = "Nome do cargo") @RequestParam String nome,
            @Parameter(description = "ID da eleição") @RequestParam UUID eleicaoId) {
        boolean exists = cargoService.existsCargoByNomeAndEleicao(nome, eleicaoId);
        return ResponseEntity.ok(exists);
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

    @GetMapping("/stats/total/eleicao/{eleicaoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de cargos por eleição", description = "Retorna o total de cargos de uma eleição")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalCargosByEleicao(@PathVariable UUID eleicaoId) {
        long total = cargoService.getTotalCargosByEleicao(eleicaoId);
        return ResponseEntity.ok(total);
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