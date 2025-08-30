package com.br.ibetelvote.application.cargo.dto;

import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCargoRequest {

    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    private UUID categoriaId;

    private HierarquiaCargo hierarquia;

    @Positive(message = "Ordem de precedência deve ser positiva")
    private Integer ordemPrecedencia;

    @Size(max = 2000, message = "Requisitos do cargo devem ter no máximo 2000 caracteres")
    private String requisitosCargo;

    @Size(min = 1, max = 10, message = "Deve ter entre 1 e 10 níveis de elegibilidade")
    private List<String> elegibilidade;

    private Boolean ativo;

    private Boolean disponivelEleicao;

    // === VALIDAÇÕES CUSTOMIZADAS ===

    /**
     * Verifica se algum campo foi alterado
     */
    public boolean temAlgumaAlteracao() {
        return nome != null ||
                descricao != null ||
                categoriaId != null ||
                hierarquia != null ||
                ordemPrecedencia != null ||
                requisitosCargo != null ||
                elegibilidade != null ||
                ativo != null ||
                disponivelEleicao != null;
    }

    /**
     * Valida se a elegibilidade contém valores válidos (se informada)
     */
    public boolean isElegibilidadeValida() {
        if (elegibilidade == null) {
            return true; // Não alterada
        }

        if (elegibilidade.isEmpty()) {
            return false; // Não pode ficar vazia
        }

        List<String> niveisValidos = List.of("MEMBRO", "OBREIRO", "DIACONO", "PRESBITERO", "PASTOR");
        return elegibilidade.stream().allMatch(niveisValidos::contains);
    }

    /**
     * Normaliza a lista de elegibilidade se foi informada
     */
    public void normalizarElegibilidade() {
        if (elegibilidade != null && !elegibilidade.isEmpty()) {
            elegibilidade = elegibilidade.stream()
                    .distinct()
                    .map(String::toUpperCase)
                    .sorted()
                    .toList();
        }
    }

    /**
     * Verifica se está tentando desativar o cargo para eleições sem desativar o cargo
     */
    public boolean isDesativandoSomenteParaEleicoes() {
        return disponivelEleicao != null && !disponivelEleicao &&
                (ativo == null || ativo);
    }

    /**
     * Verifica se está ativando para eleições mas mantendo cargo inativo
     */
    public boolean isAtivandoParaEleicoesComCargoInativo() {
        return disponivelEleicao != null && disponivelEleicao &&
                ativo != null && !ativo;
    }

    /**
     * Conta quantos campos essenciais estão sendo alterados
     */
    public int contarCamposEssenciaisAlterados() {
        int count = 0;
        if (nome != null) count++;
        if (categoriaId != null) count++;
        if (hierarquia != null) count++;
        if (elegibilidade != null) count++;
        return count;
    }

    /**
     * Verifica se é apenas uma alteração de status
     */
    public boolean isApenasMudancaDeStatus() {
        return (ativo != null || disponivelEleicao != null) &&
                nome == null &&
                categoriaId == null &&
                hierarquia == null &&
                elegibilidade == null &&
                ordemPrecedencia == null &&
                requisitosCargo == null;
    }
}