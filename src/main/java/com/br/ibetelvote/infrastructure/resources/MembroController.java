package com.br.ibetelvote.infrastructure.resources;

import com.br.ibetelvote.application.membro.dto.MembroBasicInfo;
import com.br.ibetelvote.application.membro.dto.*;
import com.br.ibetelvote.domain.services.MembroService;
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
@RequestMapping("/api/v1/membros")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Membros", description = "Endpoints para gestão de membros da igreja")
@SecurityRequirement(name = "bearerAuth")
public class MembroController {

    private final MembroService membroService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Criar membro", description = "Cria um novo membro no sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membro criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Email ou CPF já existe")
    })
    public ResponseEntity<MembroResponse> createMembro(@Valid @RequestBody CreateMembroRequest request) {
        MembroResponse response = membroService.createMembro(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }




    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar membro por ID", description = "Retorna os dados de um membro específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> getMembroById(@PathVariable UUID id) {
        MembroResponse response = membroService.getMembroById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Listar membros", description = "Lista todos os membros com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Page<MembroResponse>> getAllMembros(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<MembroResponse> response = membroService.getAllMembros(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Listar todos os membros", description = "Lista todos os membros sem paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> getAllMembrosList() {
        List<MembroResponse> response = membroService.getAllMembros();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar membro", description = "Atualiza os dados de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email ou CPF já existe")
    })
    public ResponseEntity<MembroResponse> updateMembro(@PathVariable UUID id, @Valid @RequestBody UpdateMembroRequest request) {
        MembroResponse response = membroService.updateMembro(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover membro", description = "Remove um membro do sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Membro não pode ser removido"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<Void> deleteMembro(@PathVariable UUID id) {
        membroService.deleteMembro(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Membros ativos", description = "Lista todos os membros ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<MembroResponse>> getMembrosAtivos() {
        List<MembroResponse> membros = membroService.getMembrosAtivos();
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/ativos/page")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros ativos paginados", description = "Lista membros ativos com paginação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<Page<MembroResponse>> getMembrosAtivos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<MembroResponse> membros = membroService.getMembrosAtivos(pageable);
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/inativos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Membros inativos", description = "Lista todos os membros inativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> getMembrosInativos() {
        List<MembroResponse> membros = membroService.getMembrosInativos();
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/search/nome")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar por nome", description = "Busca membros por nome (busca parcial)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> searchMembrosByNome(
            @Parameter(description = "Nome para busca") @RequestParam String nome) {
        List<MembroResponse> membros = membroService.getMembrosByNome(nome);
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/search/email/{email}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar por email", description = "Busca membro por email específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> getMembroByEmail(@PathVariable String email) {
        MembroResponse membro = membroService.getMembroByEmail(email);
        return ResponseEntity.ok(membro);
    }

    @GetMapping("/search/cpf/{cpf}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Buscar por CPF", description = "Busca membro por CPF específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> getMembroByCpf(@PathVariable String cpf) {
        MembroResponse membro = membroService.getMembroByCpf(cpf);
        return ResponseEntity.ok(membro);
    }

    @GetMapping("/cargo/{cargoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros por cargo", description = "Lista membros que possuem um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> getMembrosPorCargo(@PathVariable UUID cargoId) {
        List<MembroResponse> membros = membroService.getMembrosPorCargo(cargoId);
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/sem-cargo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros sem cargo", description = "Lista membros que não possuem cargo definido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> getMembrosSemCargo() {
        List<MembroResponse> membros = membroService.getMembrosSemCargo();
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/elegiveis/{nomeCargo}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros elegíveis", description = "Lista membros elegíveis para um cargo específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> getMembrosElegiveisParaCargo(@PathVariable String nomeCargo) {
        List<MembroResponse> membros = membroService.getMembrosElegiveisParaCargo(nomeCargo);
        return ResponseEntity.ok(membros);
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Ativar membro", description = "Ativa um membro específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> ativarMembro(@PathVariable UUID id) {
        MembroResponse response = membroService.ativarMembro(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desativar membro", description = "Desativa um membro específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Membro desativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> desativarMembro(@PathVariable UUID id) {
        MembroResponse response = membroService.desativarMembro(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cargo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Atualizar cargo do membro", description = "Atualiza o cargo de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro ou cargo não encontrado")
    })
    public ResponseEntity<MembroResponse> updateCargoMembro(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCargoMembroRequest request) {
        MembroResponse response = membroService.updateCargoMembro(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/cargo")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Remover cargo do membro", description = "Remove o cargo de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cargo removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> removeCargoMembro(@PathVariable UUID id) {
        MembroResponse response = membroService.removeCargoMembro(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Upload foto do membro", description = "Faz upload da foto de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da foto inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> uploadFotoMembro(@PathVariable UUID id, @Valid @RequestBody MembroUploadFotoRequest request) {
        MembroResponse response = membroService.uploadFotoMembro(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Remover foto do membro", description = "Remove a foto de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> removeFotoMembro(@PathVariable UUID id) {
        MembroResponse response = membroService.removeFotoMembro(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar foto do membro", description = "Retorna a foto de um membro em Base64")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Foto retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro ou foto não encontrada")
    })
    public ResponseEntity<String> getFotoMembroBase64(@PathVariable UUID id) {
        String fotoBase64 = membroService.getFotoMembroBase64(id);
        return ResponseEntity.ok(fotoBase64);
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Buscar perfil do membro", description = "Retorna o perfil completo de um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroProfileResponse> getMembroProfile(@PathVariable UUID id) {
        MembroProfileResponse profile = membroService.getMembroProfile(id);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'MEMBRO')")
    @Operation(summary = "Atualizar perfil do membro", description = "Atualiza o perfil de um membro (uso próprio)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado"),
            @ApiResponse(responseCode = "409", description = "Email já existe")
    })
    public ResponseEntity<MembroProfileResponse> updateMembroProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMembroProfileRequest request) {
        MembroProfileResponse response = membroService.updateMembroProfile(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Validar membro", description = "Valida dados de um membro (email e CPF)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validação realizada"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ValidarMembroResponse> validarMembro(@Valid @RequestBody ValidarMembroRequest request) {
        ValidarMembroResponse response = membroService.validarMembro(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/elegibilidade")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Verificar elegibilidade", description = "Verifica elegibilidade do membro para eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Elegibilidade verificada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroElegibilidadeResponse> verificarElegibilidade(
            @PathVariable UUID id,
            @Parameter(description = "Lista de cargos disponíveis") @RequestParam(required = false) List<String> cargosDisponiveis) {
        MembroElegibilidadeResponse response = membroService.verificarElegibilidade(id, cargosDisponiveis);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/aptos-votacao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Membros aptos para votação", description = "Lista membros aptos para participar de eleições")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> getMembrosAptosParaVotacao() {
        List<MembroResponse> membros = membroService.getMembrosAptosParaVotacao();
        return ResponseEntity.ok(membros);
    }

    @PostMapping("/filtros")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Buscar com filtros", description = "Busca membros aplicando filtros específicos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtros inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Page<MembroListResponse>> buscarMembrosComFiltros(@Valid @RequestBody MembroFilterRequest filtros,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<MembroListResponse> response = membroService.buscarMembrosComFiltros(filtros, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/listagem")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Listagem simples", description = "Lista membros para exibição simples")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<MembroListResponse>> getMembrosParaListagem() {
        List<MembroListResponse> membros = membroService.getMembrosParaListagem();
        return ResponseEntity.ok(membros);
    }


    @GetMapping("/disponivel/email")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar disponibilidade do email", description = "Verifica se email está disponível")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> isEmailDisponivel(
            @Parameter(description = "Email para verificar") @RequestParam String email) {
        boolean disponivel = membroService.isEmailDisponivel(email);
        return ResponseEntity.ok(disponivel);
    }

    @GetMapping("/disponivel/cpf")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar disponibilidade do CPF", description = "Verifica se CPF está disponível")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Boolean> isCpfDisponivel(
            @Parameter(description = "CPF para verificar") @RequestParam String cpf) {
        boolean disponivel = membroService.isCpfDisponivel(cpf);
        return ResponseEntity.ok(disponivel);
    }

    @GetMapping("/{id}/can-delete")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Verificar se pode deletar", description = "Verifica se um membro pode ser removido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verificação realizada"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<Boolean> canDeleteMembro(@PathVariable UUID id) {
        boolean canDelete = membroService.canDeleteMembro(id);
        return ResponseEntity.ok(canDelete);
    }

    @PutMapping("/{membroId}/usuario/{userId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Associar usuário", description = "Associa um usuário a um membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário associado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Usuário já associado ou membro não pode ter usuário"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> associarUsuario(@PathVariable UUID membroId, @PathVariable UUID userId) {
        MembroResponse response = membroService.associarUsuario(membroId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{membroId}/usuario")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Desassociar usuário", description = "Remove a associação entre usuário e membro")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário desassociado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Membro não encontrado")
    })
    public ResponseEntity<MembroResponse> desassociarUsuario(@PathVariable UUID membroId) {
        MembroResponse response = membroService.desassociarUsuario(membroId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/podem-criar-usuario")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Membros que podem criar usuário", description = "Lista membros que podem ter usuário criado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<List<MembroResponse>> getMembrosQuePodemCriarUsuario() {
        List<MembroResponse> membros = membroService.getMembrosQuePodemCriarUsuario();
        return ResponseEntity.ok(membros);
    }

    @GetMapping("/stats/total")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de membros", description = "Retorna o total de membros cadastrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembros() {
        long total = membroService.getTotalMembros();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/ativos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total de membros ativos", description = "Retorna o total de membros ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosAtivos() {
        long total = membroService.getTotalMembrosAtivos();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/inativos")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Total de membros inativos", description = "Retorna o total de membros inativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosInativos() {
        long total = membroService.getTotalMembrosInativos();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/cargo/{cargoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total por cargo", description = "Retorna o total de membros por cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosPorCargo(@PathVariable UUID cargoId) {
        long total = membroService.getTotalMembrosPorCargo(cargoId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/sem-cargo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total sem cargo", description = "Retorna o total de membros sem cargo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosSemCargo() {
        long total = membroService.getTotalMembrosSemCargo();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/stats/aptos-votacao")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO')")
    @Operation(summary = "Total aptos para votação", description = "Retorna o total de membros aptos para votação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Long> getTotalMembrosAptosParaVotacao() {
        long total = membroService.getTotalMembrosAptosParaVotacao();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/basic-info")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'UTILIZADOR_PRO', 'MEMBRO')")
    @Operation(summary = "Informações básicas", description = "Retorna informações básicas dos membros ativos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Informações retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado")
    })
    public ResponseEntity<List<MembroBasicInfo>> getMembrosBasicInfo() {
        List<MembroBasicInfo> basicInfo = membroService.getMembrosBasicInfo();
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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e, HttpServletRequest request) {
        log.error("Erro de estado inválido: {}", e.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_STATE")
                .message(e.getMessage())
                .path(request.getRequestURI())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
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