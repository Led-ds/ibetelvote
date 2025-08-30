package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.auth.dto.*;
import com.br.ibetelvote.domain.entities.enums.UserRole;
import com.br.ibetelvote.domain.services.UserService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Usuários", description = "Endpoints para gestão de usuários do sistema")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    // === OPERAÇÕES BÁSICAS ===

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar usuários", description = "Lista todos os usuários do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenação") @RequestParam(defaultValue = "email") String sort,
            @Parameter(description = "Direção da ordenação") @RequestParam(defaultValue = "asc") String direction
    ) {
        Page<UserResponse> users = userService.getAllUsers(page, size, sort, direction);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar usuário", description = "Cria um novo usuário do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Buscar usuário por ID", description = "Retorna os dados de um usuário específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover usuário", description = "Remove um usuário do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // === OPERAÇÕES DE CONTROLE DE CONTA ===

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar usuário", description = "Ativa um usuário desativado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> activateUser(@PathVariable UUID id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar usuário", description = "Desativa um usuário ativo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário desativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Bloquear usuário", description = "Bloqueia o acesso de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário bloqueado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> lockUser(@PathVariable UUID id) {
        userService.lockUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desbloquear usuário", description = "Desbloqueia o acesso de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário desbloqueado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> unlockUser(@PathVariable UUID id) {
        userService.unlockUser(id);
        return ResponseEntity.ok().build();
    }

    // === OPERAÇÕES DE ROLE ===

    @PostMapping("/{id}/change-role")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Alterar role", description = "Altera a role/permissão de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> changeUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeRoleRequest request) {
        userService.changeUserRole(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Usuários por role", description = "Lista usuários ativos de uma role específica")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    // === OPERAÇÕES DE SENHA ===

    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasRole('ADMINISTRADOR') or @userService.getUserById(#id).email == authentication.name")
    @Operation(summary = "Alterar senha", description = "Altera a senha de um usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Resetar senha", description = "Define uma nova senha para o usuário (apenas administradores)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha resetada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<Void> resetPassword(
            @PathVariable UUID id,
            @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    // === CONSULTAS ESPECÍFICAS ===

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Buscar usuário por email", description = "Retorna os dados de um usuário pelo email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    // === ESTATÍSTICAS ===

    @GetMapping("/stats/total")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Total de usuários", description = "Retorna o total de usuários cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalUsers() {
        long total = userService.getTotalUsers();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/active")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Total de usuários ativos", description = "Retorna o total de usuários ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalActiveUsers() {
        long total = userService.getTotalActiveUsers();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/by-role/{role}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Total por role", description = "Retorna o total de usuários ativos de uma role")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalUsersByRole(@PathVariable UserRole role) {
        long total = userService.getTotalUsersByRole(role);
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

    // DTO para reset de senha
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class ResetPasswordRequest {
        @jakarta.validation.constraints.NotBlank(message = "Nova senha é obrigatória")
        @jakarta.validation.constraints.Size(min = 6, message = "Nova senha deve ter no mínimo 6 caracteres")
        private String newPassword;
    }
}