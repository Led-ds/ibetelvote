package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.auth.dto.LoginResponse;
import com.br.ibetelvote.application.membro.dto.CreateUserByMembroRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.br.ibetelvote.application.membro.dto.MembroProfileResponse;
import com.br.ibetelvote.application.membro.dto.UpdateMembroProfileRequest;
import com.br.ibetelvote.application.membro.dto.ValidarMembroRequest;
import com.br.ibetelvote.application.membro.dto.ValidarMembroResponse;
import com.br.ibetelvote.application.shared.dto.UploadPhotoResponse;
import com.br.ibetelvote.domain.services.AutoCadastroService;
import com.br.ibetelvote.domain.services.MembroService;
import com.br.ibetelvote.infrastructure.jwt.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller responsável pelo auto-cadastro de membros no sistema.
 * Gerencia a validação, criação de usuários e perfil dos membros.
 */
@RestController
@RequestMapping("/api/v1/auto-cadastro")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auto-cadastro", description = "Endpoints para auto-cadastro de membros")
public class AutoCadastroController {

    private final AutoCadastroService autoCadastroService;
    private final MembroService membroService;
    private final JwtService jwtService;

    /**
     * Valida se um membro pode criar usuário no sistema
     */
    @PostMapping("/validar-membro")
    @Operation(
            summary = "Validar membro",
            description = "Valida se um membro pode criar usuário baseado em email e CPF"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro validado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou membro não pode criar usuário"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<ValidarMembroResponse> validarMembro(
            @Valid @RequestBody ValidarMembroRequest request) {
        log.info("Validando membro com email: {}", request.getEmail());
        ValidarMembroResponse response = autoCadastroService.validarMembro(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cria um novo usuário para membro validado
     */
    @PostMapping("/criar-usuario")
    @Operation(
            summary = "Criar usuário",
            description = "Cria usuário para membro previamente validado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senhas não coincidem"),
            @ApiResponse(responseCode = "409", description = "Email já em uso")
    })
    public ResponseEntity<LoginResponse> criarUsuario(
            @Valid @RequestBody CreateUserCompleteRequest request) {

        log.info("Criando usuário para email: {}", request.getEmail());

        // Validar se as senhas coincidem usando o método do DTO
        if (!request.isPasswordsMatch()) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        // Usar os métodos de conversão do próprio DTO
        ValidarMembroRequest dadosMembro = request.toValidarMembroRequest();
        CreateUserByMembroRequest dadosUsuario = request.toCreateUserByMembroRequest();

        LoginResponse response = autoCadastroService.createUserByMembro(dadosMembro, dadosUsuario);
        log.info("Usuário criado com sucesso para email: {}", request.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verifica se um email/CPF podem criar usuário
     */
    @GetMapping("/verificar-elegibilidade")
    @Operation(
            summary = "Verificar elegibilidade",
            description = "Verifica se email/CPF são elegíveis para criar usuário"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos")
    })
    public ResponseEntity<ElegibilidadeResponse> verificarElegibilidade(
            @Parameter(description = "Email do membro", required = true)
            @RequestParam @Email(message = "Email inválido") String email,

            @Parameter(description = "CPF do membro (com ou sem formatação)", required = true)
            @RequestParam @NotBlank(message = "CPF é obrigatório") String cpf) {

        log.debug("Verificando elegibilidade para email: {} e CPF: {}", email, cpf);

        // Limpar CPF antes de verificar (remover formatação)
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");

        boolean podeCrear = autoCadastroService.canMembroCreateUser(email, cpfLimpo);

        ElegibilidadeResponse response = ElegibilidadeResponse.builder()
                .elegivel(podeCrear)
                .message(podeCrear
                        ? "Membro elegível para criar usuário"
                        : "Membro não elegível para criar usuário")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Retorna o perfil completo do membro autenticado
     */
    @GetMapping("/meu-perfil")
    @PreAuthorize("hasAnyRole('MEMBRO', 'UTILIZADOR_PRO', 'ADMINISTRADOR')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Obter meu perfil",
            description = "Retorna o perfil completo do membro autenticado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroProfileResponse> obterMeuPerfil(Authentication authentication) {
        UUID userId = extrairMembroIdDoToken(authentication);
        log.info("Obtendo perfil do membro: {}", userId);

        MembroProfileResponse response = autoCadastroService.getMembroProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza o perfil do membro autenticado
     */
    @PutMapping("/meu-perfil")
    @PreAuthorize("hasAnyRole('MEMBRO', 'UTILIZADOR_PRO', 'ADMINISTRADOR')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Atualizar meu perfil",
            description = "Atualiza o perfil do membro autenticado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroProfileResponse> atualizarMeuPerfil(
            @Valid @RequestBody UpdateMembroProfileRequest request,
            Authentication authentication) {

        UUID userId = extrairMembroIdDoToken(authentication);
        log.info("Atualizando perfil do membro: {}", userId);

        MembroProfileResponse response = autoCadastroService.updateMembroProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload de foto do perfil do membro autenticado
     */
    @PostMapping(value = "/meu-perfil/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MEMBRO', 'UTILIZADOR_PRO', 'ADMINISTRADOR')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Upload de foto do perfil",
            description = "Faz upload da foto do perfil do membro autenticado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou formato não suportado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "413", description = "Arquivo muito grande"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<UploadPhotoResponse> uploadFotoPerfil(
            @Parameter(description = "Arquivo de imagem (JPG, PNG, WEBP) - Máx 5MB", required = true)
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        UUID userId = extrairMembroIdDoToken(authentication);
        log.info("Upload de foto para membro: {}, arquivo: {}, tamanho: {} bytes",
                userId, file.getOriginalFilename(), file.getSize());

        UploadPhotoResponse response = membroService.uploadPhoto(userId, file);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove a foto do perfil do membro autenticado
     */
    @DeleteMapping("/meu-perfil/foto")
    @PreAuthorize("hasAnyRole('MEMBRO', 'UTILIZADOR_PRO', 'ADMINISTRADOR')")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Remover foto do perfil",
            description = "Remove a foto do perfil do membro autenticado"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado ou sem foto")
    })
    public ResponseEntity<Void> removerFotoPerfil(Authentication authentication) {
        UUID membroId = extrairMembroIdDoToken(authentication);
        log.info("Removendo foto do membro: {}", membroId);

        membroService.removePhoto(membroId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extrai o ID do membro do token JWT
     *
     * @param authentication Objeto de autenticação do Spring Security
     * @return UUID do membro
     * @throws IllegalStateException se não conseguir extrair o ID
     */
    private UUID extrairMembroIdDoToken(Authentication authentication) {
        try {
            // Obter token JWT da requisição
            String token = obterTokenJwtAtual();

            // Extrair ID do usuário do token
            UUID userId = jwtService.extractUserId(token);

            // TODO: Implementar lógica para obter membroId a partir do userId
            // Por enquanto, assumindo que userId = membroId
            // Em produção, você deve buscar o membro associado ao usuário

            log.debug("ID extraído do token: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("Erro ao extrair ID do membro do token", e);
            throw new IllegalStateException("Não foi possível identificar o membro autenticado");
        }
    }

    /**
     * Obtém o token JWT da requisição atual
     *
     * @return Token JWT sem o prefixo "Bearer "
     * @throws IllegalStateException se não encontrar o token
     */
    private String obterTokenJwtAtual() {
        HttpServletRequest request = obterRequisicaoHttp()
                .orElseThrow(() -> new IllegalStateException("Requisição HTTP não encontrada"));

        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            // Tentar obter do SecurityContext como fallback
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getCredentials() != null) {
                return auth.getCredentials().toString();
            }
            throw new IllegalStateException("Token JWT não encontrado na requisição");
        }

        return bearerToken.substring(7);
    }

    /**
     * Obtém a requisição HTTP atual do contexto
     *
     * @return Optional contendo a requisição HTTP
     */
    private Optional<HttpServletRequest> obterRequisicaoHttp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return Optional.ofNullable(attributes.getRequest());
        } catch (Exception e) {
            log.warn("Não foi possível obter requisição HTTP do contexto", e);
            return Optional.empty();
        }
    }

    /**
     * Request para criação completa de usuário via auto-cadastro
     * Combina validação de membro + criação de usuário
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserCompleteRequest {

        // === DADOS PARA VALIDAÇÃO DO MEMBRO ===

        @Email(message = "Email deve ser válido")
        @NotBlank(message = "Email é obrigatório")
        @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
        private String email;

        @NotBlank(message = "CPF é obrigatório")
        @Size(min = 11, max = 14, message = "CPF deve ter entre 11 e 14 caracteres")
        private String cpf;

        // === DADOS PARA CRIAÇÃO DO USUÁRIO ===

        // ✅ MAPEAMENTO CORRETO: frontend envia "senha", backend recebe como "password"
        @JsonProperty("senha")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, max = 50, message = "Senha deve ter entre 6 e 50 caracteres")
        private String password;

        // ✅ MAPEAMENTO CORRETO: frontend envia "confirmarSenha", backend recebe como "confirmPassword"
        @JsonProperty("confirmarSenha")
        @NotBlank(message = "Confirmação de senha é obrigatória")
        private String confirmPassword;

        // === MÉTODOS DE VALIDAÇÃO ===

        /**
         * Verifica se as senhas coincidem
         */
        public boolean isPasswordsMatch() {
            return password != null && password.equals(confirmPassword);
        }

        /**
         * Limpa e formata o CPF removendo caracteres especiais
         */
        public String getCleanCpf() {
            if (cpf == null) return null;
            return cpf.replaceAll("[^0-9]", "");
        }

        /**
         * Formata o CPF para o padrão XXX.XXX.XXX-XX
         */
        public String getFormattedCpf() {
            String clean = getCleanCpf();
            if (clean == null || clean.length() != 11) {
                return cpf; // Retorna original se inválido
            }
            return clean.substring(0, 3) + "." +
                    clean.substring(3, 6) + "." +
                    clean.substring(6, 9) + "-" +
                    clean.substring(9);
        }

        /**
         * Converte para ValidarMembroRequest
         */
        public ValidarMembroRequest toValidarMembroRequest() {
            return ValidarMembroRequest.builder()
                    .email(this.email)
                    .cpf(this.getCleanCpf()) // Usar CPF limpo
                    .build();
        }

        /**
         * Converte para CreateUserByMembroRequest
         */
        public CreateUserByMembroRequest toCreateUserByMembroRequest() {
            return CreateUserByMembroRequest.builder()
                    .password(this.password)
                    .confirmPassword(this.confirmPassword)
                    .build();
        }
    }

    /**
     * Response de elegibilidade para criação de usuário
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ElegibilidadeResponse {
        private boolean elegivel;
        private String message;
    }

    /**
     * Response padrão para erros
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ErrorResponse {
        private String code;
        private String message;
        private String path;
        private LocalDateTime timestamp;
    }

    // ========================================
    // EXCEPTION HANDLERS
    // ========================================

    /**
     * Handler para exceções de argumentos inválidos
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e,
            HttpServletRequest request) {

        log.warn("Argumento inválido: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_REQUEST")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handler para exceções de estado inválido
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException e,
            HttpServletRequest request) {

        log.warn("Estado inválido: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_STATE")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Handler genérico para exceções não tratadas
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException e,
            HttpServletRequest request) {

        log.error("Erro interno no AutoCadastroController: {}", e.getMessage(), e);

        ErrorResponse error = ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Erro interno do servidor. Por favor, tente novamente mais tarde.")
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}