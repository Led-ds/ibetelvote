package com.br.ibetelvote.application.cargo.dto;

import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCargoRequest {

    @NotBlank(message = "Nome do cargo é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "Categoria é obrigatória")
    private UUID categoriaId;

    @NotNull(message = "Hierarquia é obrigatória")
    private HierarquiaCargo hierarquia;

    @Positive(message = "Ordem de precedência deve ser positiva")
    private Integer ordemPrecedencia;

    @Size(max = 2000, message = "Requisitos do cargo devem ter no máximo 2000 caracteres")
    private String requisitosCargo;

    @NotEmpty(message = "Pelo menos um nível de elegibilidade deve ser informado")
    @Size(min = 1, max = 10, message = "Deve ter entre 1 e 10 níveis de elegibilidade")
    @Builder.Default
    private List<String> elegibilidade = new ArrayList<>();

    @Builder.Default
    private Boolean ativo = true;

    @Builder.Default
    private Boolean disponivelEleicao = true;

    // === VALIDAÇÕES CUSTOMIZADAS ===

    /**
     * Valida se a elegibilidade contém valores válidos
     */
    public boolean isElegibilidadeValida() {
        if (elegibilidade == null || elegibilidade.isEmpty()) {
            return false;
        }

        List<String> niveisValidos = List.of("MEMBRO", "OBREIRO", "DIACONO", "PRESBITERO", "PASTOR");
        return elegibilidade.stream().allMatch(niveisValidos::contains);
    }

    /**
     * Valida se a hierarquia é compatível com a elegibilidade
     */
    public boolean isHierarquiaCompativelComElegibilidade() {
        if (hierarquia == null || elegibilidade == null || elegibilidade.isEmpty()) {
            return true; // Validação será feita no service
        }

        // Regras específicas de compatibilidade
        return switch (hierarquia) {
            case PASTORAL -> elegibilidade.contains("PRESBITERO") || elegibilidade.contains("PASTOR");
            case PRESBITERAL -> elegibilidade.contains("DIACONO") ||
                    elegibilidade.contains("PRESBITERO") ||
                    elegibilidade.contains("PASTOR");
            case DIACONAL -> elegibilidade.contains("MEMBRO") ||
                    elegibilidade.contains("OBREIRO") ||
                    elegibilidade.contains("DIACONO");
            case LIDERANCA -> true; // Aceita qualquer elegibilidade
            case AUXILIAR -> elegibilidade.contains("MEMBRO") || elegibilidade.contains("OBREIRO");
            case ADMINISTRATIVO -> true; // Aceita qualquer elegibilidade
        };
    }

    /**
     * Normaliza a lista de elegibilidade removendo duplicatas e ordenando
     */
    public void normalizarElegibilidade() {
        if (elegibilidade != null) {
            elegibilidade = elegibilidade.stream()
                    .distinct()
                    .map(String::toUpperCase)
                    .sorted()
                    .toList();
        }
    }

    /**
     * Valida se o cargo pode ser disponibilizado para eleições
     */
    public boolean podeSerDisponibilizadoParaEleicoes() {
        return ativo != null && ativo &&
                nome != null && !nome.trim().isEmpty() &&
                descricao != null && !descricao.trim().isEmpty() &&
                categoriaId != null &&
                hierarquia != null &&
                elegibilidade != null && !elegibilidade.isEmpty();
    }
}