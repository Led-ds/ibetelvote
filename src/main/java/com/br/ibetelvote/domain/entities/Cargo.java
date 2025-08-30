package com.br.ibetelvote.domain.entities;

import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade representando os cargos ministeriais da igreja.
 *
 * Cada cargo possui uma categoria organizacional, hierarquia eclesiástica,
 * requisitos específicos e regras de elegibilidade definidas.
 */
@Entity
@Table(name = "cargos", indexes = {
        @Index(name = "idx_cargo_nome", columnList = "nome"),
        @Index(name = "idx_cargo_ativo", columnList = "ativo"),
        @Index(name = "idx_cargo_categoria", columnList = "categoria_id"),
        @Index(name = "idx_cargo_hierarquia", columnList = "hierarquia"),
        @Index(name = "idx_cargo_disponivel_eleicao", columnList = "disponivel_eleicao"),
        @Index(name = "idx_cargo_ordem_precedencia", columnList = "ordem_precedencia")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Cargo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Nome do cargo é obrigatório")
    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Builder.Default
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    @JsonIgnore
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "hierarquia", nullable = false)
    @Builder.Default
    private HierarquiaCargo hierarquia = HierarquiaCargo.AUXILIAR;

    @Positive(message = "Ordem de precedência deve ser positiva")
    @Column(name = "ordem_precedencia")
    private Integer ordemPrecedencia;

    @Column(name = "requisitos_cargo", columnDefinition = "TEXT")
    private String requisitosCargo;

    /**
     * Array JSON com os cargos que podem se candidatar a este cargo
     * Exemplo: ["MEMBRO", "OBREIRO", "DIACONO"]
     * Usando conversão manual com Jackson para JSONB
     */
    @Column(name = "elegibilidade", columnDefinition = "jsonb")
    @Builder.Default
    private String elegibilidadeJson = "[]";

    /**
     * Indica se o cargo está disponível para eleições
     */
    /**
     * Indica se o cargo está disponível para eleições
     */
    @Builder.Default
    @Column(name = "disponivel_eleicao", nullable = false)
    private Boolean disponivelEleicao = true;

    // === CAMPOS TRANSIENTES PARA ELEGIBILIDADE ===

    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Transient
    private List<String> elegibilidade;

    // === MÉTODOS PARA CONVERSÃO JSON ===

    /**
     * Converte lista de elegibilidade para JSON antes de persistir
     */
    @PrePersist
    @PreUpdate
    public void convertElegibilidadeToJson() {
        try {
            if (elegibilidade != null && !elegibilidade.isEmpty()) {
                this.elegibilidadeJson = objectMapper.writeValueAsString(elegibilidade);
            } else {
                this.elegibilidadeJson = "[]";
            }
        } catch (JsonProcessingException e) {
            this.elegibilidadeJson = "[]";
        }
    }

    /**
     * Converte JSON para lista de elegibilidade após carregar
     */
    @PostLoad
    public void convertJsonToElegibilidade() {
        try {
            if (elegibilidadeJson != null && !elegibilidadeJson.trim().isEmpty() && !"null".equals(elegibilidadeJson)) {
                TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {};
                this.elegibilidade = objectMapper.readValue(elegibilidadeJson, typeRef);
            } else {
                this.elegibilidade = new ArrayList<>();
            }
        } catch (JsonProcessingException e) {
            this.elegibilidade = new ArrayList<>();
        }

        if (this.elegibilidade == null) {
            this.elegibilidade = new ArrayList<>();
        }
    }

    /**
     * Getter para elegibilidade (inicializa se necessário)
     */
    public List<String> getElegibilidade() {
        if (this.elegibilidade == null) {
            convertJsonToElegibilidade();
        }
        return this.elegibilidade;
    }

    /**
     * Setter para elegibilidade (converte para JSON)
     */
    public void setElegibilidade(List<String> elegibilidade) {
        this.elegibilidade = elegibilidade != null ? elegibilidade : new ArrayList<>();
        convertElegibilidadeToJson();
    }

    // === MÉTODOS DE NEGÓCIO ===

    /**
     * Atualiza as informações básicas do cargo
     */
    public void updateBasicInfo(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    /**
     * Atualiza informações completas do cargo
     */
    public void updateCompleteInfo(String nome, String descricao, Categoria categoria,
                                   HierarquiaCargo hierarquia, Integer ordemPrecedencia,
                                   String requisitosCargo, List<String> elegibilidade,
                                   Boolean disponivelEleicao) {
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.hierarquia = hierarquia != null ? hierarquia : HierarquiaCargo.AUXILIAR;
        this.ordemPrecedencia = ordemPrecedencia;
        this.requisitosCargo = requisitosCargo;
        setElegibilidade(elegibilidade); // Usa o setter que converte para JSON
        this.disponivelEleicao = disponivelEleicao != null ? disponivelEleicao : true;
    }

    /**
     * Ativa o cargo
     */
    public void activate() {
        this.ativo = true;
    }

    /**
     * Desativa o cargo
     */
    public void deactivate() {
        this.ativo = false;
    }

    /**
     * Ativa disponibilidade para eleições
     */
    public void ativarParaEleicao() {
        this.disponivelEleicao = true;
    }

    /**
     * Desativa disponibilidade para eleições
     */
    public void desativarParaEleicao() {
        this.disponivelEleicao = false;
    }

    /**
     * Verifica se o cargo está ativo
     */
    public boolean isAtivo() {
        return ativo != null && ativo;
    }

    /**
     * Verifica se está disponível para eleições
     */
    public boolean isDisponivelEleicao() {
        return disponivelEleicao != null && disponivelEleicao;
    }

    /**
     * Verifica se o cargo pode ser usado em eleições
     */
    public boolean podeSerUsadoEmEleicoes() {
        return isAtivo() && isDisponivelEleicao() &&
                nome != null && !nome.trim().isEmpty() &&
                temInformacoesCompletas();
    }

    /**
     * Verifica se um candidato específico pode se candidatar a este cargo
     */
    public boolean podeReceberCandidaturaDeCargо(String cargoOrigem) {
        List<String> elegibilidades = getElegibilidade();
        return elegibilidades != null && elegibilidades.contains(cargoOrigem);
    }

    /**
     * Verifica se uma hierarquia pode se candidatar a este cargo
     */
    public boolean podeReceberCandidaturaDaHierarquia(HierarquiaCargo hierarquiaOrigem) {
        return hierarquiaOrigem != null && hierarquiaOrigem.podeElegerPara(this.hierarquia);
    }

    /**
     * Adiciona um cargo à lista de elegibilidade
     */
    public void adicionarElegibilidade(String cargo) {
        if (cargo != null && !cargo.trim().isEmpty()) {
            List<String> elegibilidades = getElegibilidade();
            if (!elegibilidades.contains(cargo.toUpperCase())) {
                elegibilidades.add(cargo.toUpperCase());
                setElegibilidade(elegibilidades);
            }
        }
    }

    /**
     * Remove um cargo da lista de elegibilidade
     */
    public void removerElegibilidade(String cargo) {
        if (cargo != null) {
            List<String> elegibilidades = getElegibilidade();
            if (elegibilidades.remove(cargo.toUpperCase())) {
                setElegibilidade(elegibilidades);
            }
        }
    }

    /**
     * Define lista completa de elegibilidade
     */
    public void setElegibilidadeCompleta(List<String> novaElegibilidade) {
        if (novaElegibilidade == null) {
            setElegibilidade(new ArrayList<>());
        } else {
            List<String> elegibilidadeLimpa = novaElegibilidade.stream()
                    .filter(cargo -> cargo != null && !cargo.trim().isEmpty())
                    .map(String::toUpperCase)
                    .distinct()
                    .toList();
            setElegibilidade(new ArrayList<>(elegibilidadeLimpa));
        }
    }

    // === GETTERS COMPUTADOS ===

    /**
     * Retorna o nome da categoria
     */
    public String getCategoriaNome() {
        return categoria != null ? categoria.getNome() : "Sem categoria";
    }

    /**
     * Retorna o ID da categoria
     */
    public UUID getCategoriaId() {
        return categoria != null ? categoria.getId() : null;
    }

    /**
     * Retorna o nome da hierarquia para exibição
     */
    public String getHierarquiaDisplayName() {
        return hierarquia != null ? hierarquia.getDisplayName() : "Não definida";
    }

    /**
     * Retorna o nome para exibição
     */
    public String getDisplayName() {
        return nome;
    }

    /**
     * Retorna uma descrição resumida do cargo
     */
    public String getResumo() {
        if (descricao != null && !descricao.trim().isEmpty()) {
            return descricao.length() > 100 ?
                    descricao.substring(0, 97) + "..." :
                    descricao;
        }
        return "Sem descrição";
    }

    /**
     * Retorna resumo dos requisitos
     */
    public String getRequisitoResumo() {
        if (requisitosCargo != null && !requisitosCargo.trim().isEmpty()) {
            return requisitosCargo.length() > 80 ?
                    requisitosCargo.substring(0, 77) + "..." :
                    requisitosCargo;
        }
        return "Sem requisitos definidos";
    }

    /**
     * Retorna o status do cargo
     */
    public String getStatus() {
        if (!isAtivo()) {
            return "Inativo";
        }
        if (!isDisponivelEleicao()) {
            return "Ativo (Não elegível)";
        }
        return "Ativo";
    }

    /**
     * Retorna string com elegibilidade formatada
     */
    public String getElegibilidadeFormatada() {
        List<String> elegibilidades = getElegibilidade();
        if (elegibilidades == null || elegibilidades.isEmpty()) {
            return "Não definida";
        }
        return String.join(", ", elegibilidades);
    }

    /**
     * Verifica se o cargo tem informações completas
     */
    public boolean temInformacoesCompletas() {
        List<String> elegibilidades = getElegibilidade();
        return isNomeValido(this.nome) &&
                this.descricao != null && !this.descricao.trim().isEmpty() &&
                this.hierarquia != null &&
                this.categoria != null &&
                elegibilidades != null && !elegibilidades.isEmpty();
    }

    /**
     * Verifica se tem categoria associada
     */
    public boolean temCategoria() {
        return categoria != null;
    }

    /**
     * Retorna estatísticas do cargo (pode ser usado para relatórios)
     */
    public String getEstatisticasResumo() {
        StringBuilder stats = new StringBuilder();
        stats.append("Status: ").append(getStatus());
        if (categoria != null) {
            stats.append(" | Categoria: ").append(categoria.getNome());
        }
        if (hierarquia != null) {
            stats.append(" | Hierarquia: ").append(hierarquia.getDisplayName());
        }
        if (ordemPrecedencia != null) {
            stats.append(" | Ordem: ").append(ordemPrecedencia);
        }
        return stats.toString();
    }

    /**
     * Verifica se pode ser promovido para uma hierarquia superior
     */
    public boolean podeSerPromovidoPara(HierarquiaCargo novaHierarquia) {
        if (novaHierarquia == null || this.hierarquia == null) {
            return false;
        }
        return this.hierarquia.ehIgualOuSuperiorA(novaHierarquia) &&
                novaHierarquia != this.hierarquia;
    }

    /**
     * Retorna a próxima hierarquia possível (para sugerir promoção)
     */
    public HierarquiaCargo getProximaHierarquiaSugerida() {
        return hierarquia != null ? hierarquia.getProximaHierarquia() : null;
    }

    // === MÉTODOS UTILITÁRIOS ESTÁTICOS ===

    /**
     * Valida se o nome do cargo é válido
     */
    public static boolean isNomeValido(String nome) {
        return nome != null &&
                !nome.trim().isEmpty() &&
                nome.trim().length() >= 3 &&
                nome.trim().length() <= 100;
    }

    /**
     * Normaliza o nome do cargo (primeira letra maiúscula de cada palavra)
     */
    public static String normalizarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return nome;
        }

        String nomeNormalizado = nome.trim();
        StringBuilder resultado = new StringBuilder();
        String[] palavras = nomeNormalizado.toLowerCase().split("\\s+");

        for (int i = 0; i < palavras.length; i++) {
            if (i > 0) {
                resultado.append(" ");
            }
            if (palavras[i].length() > 0) {
                resultado.append(Character.toUpperCase(palavras[i].charAt(0)));
                if (palavras[i].length() > 1) {
                    resultado.append(palavras[i].substring(1));
                }
            }
        }

        return resultado.toString();
    }

    /**
     * Valida se a ordem de precedência é válida
     */
    public static boolean isOrdemPrecedenciaValida(Integer ordem) {
        return ordem == null || ordem > 0;
    }

    /**
     * Valida se os requisitos são válidos
     */
    public static boolean isRequisitosValido(String requisitos) {
        return requisitos == null || requisitos.trim().length() <= 2000;
    }

    /**
     * Valida lista de elegibilidade
     */
    public static boolean isElegibilidadeValida(List<String> elegibilidade) {
        if (elegibilidade == null) {
            return true;
        }

        // Verificar se todos os itens são válidos
        for (String item : elegibilidade) {
            if (item == null || item.trim().isEmpty()) {
                return false;
            }
        }

        // Verificar se não há duplicatas
        long distinct = elegibilidade.stream().distinct().count();
        return distinct == elegibilidade.size();
    }

    // === BUILDERS PERSONALIZADOS ===

    /**
     * Builder para cargo ativo básico
     */
    public static CargoBuilder ativo(String nome) {
        return Cargo.builder()
                .nome(normalizarNome(nome))
                .ativo(true)
                .disponivelEleicao(true)
                .hierarquia(HierarquiaCargo.AUXILIAR)
                .elegibilidade(new ArrayList<>());
    }

    /**
     * Builder para cargo com categoria e hierarquia
     */
    public static CargoBuilder comCategoriaEHierarquia(String nome, Categoria categoria,
                                                       HierarquiaCargo hierarquia) {
        return Cargo.builder()
                .nome(normalizarNome(nome))
                .categoria(categoria)
                .hierarquia(hierarquia)
                .ativo(true)
                .disponivelEleicao(true)
                .elegibilidade(new ArrayList<>());
    }

    /**
     * Builder para cargo completo
     */
    public static CargoBuilder completo(String nome, String descricao, Categoria categoria,
                                        HierarquiaCargo hierarquia, Integer ordem,
                                        String requisitos, List<String> elegibilidade) {
        CargoBuilder builder = Cargo.builder()
                .nome(normalizarNome(nome))
                .descricao(descricao)
                .categoria(categoria)
                .hierarquia(hierarquia)
                .ordemPrecedencia(ordem)
                .requisitosCargo(requisitos)
                .ativo(true)
                .disponivelEleicao(true);

        // Configurar elegibilidade via JSON
        if (elegibilidade != null && !elegibilidade.isEmpty()) {
            try {
                String elegibilidadeJson = objectMapper.writeValueAsString(elegibilidade);
                builder.elegibilidadeJson(elegibilidadeJson);
            } catch (JsonProcessingException e) {
                builder.elegibilidadeJson("[]");
            }
        } else {
            builder.elegibilidadeJson("[]");
        }

        return builder;
    }

    // === MÉTODOS DE COMPARAÇÃO ===

    /**
     * Compara cargos por precedência (categoria, ordem, nome)
     */
    public int compareByPrecedencia(Cargo outro) {
        if (outro == null) return 1;

        // Primeiro por categoria (ordem de exibição)
        if (this.categoria != null && outro.categoria != null) {
            int categoriaComp = this.categoria.getOrdemExibicao()
                    .compareTo(outro.categoria.getOrdemExibicao());
            if (categoriaComp != 0) return categoriaComp;
        }

        // Depois por ordem de precedência
        if (this.ordemPrecedencia != null && outro.ordemPrecedencia != null) {
            int ordemComp = this.ordemPrecedencia.compareTo(outro.ordemPrecedencia);
            if (ordemComp != 0) return ordemComp;
        }

        // Por último por nome
        return this.nome.compareTo(outro.nome);
    }

    /**
     * Compara cargos por hierarquia
     */
    public int compareByHierarquia(Cargo outro) {
        if (outro == null) return 1;

        if (this.hierarquia != null && outro.hierarquia != null) {
            int hierarquiaComp = this.hierarquia.getOrdem().compareTo(outro.hierarquia.getOrdem());
            if (hierarquiaComp != 0) return hierarquiaComp;
        }

        return this.nome.compareTo(outro.nome);
    }

    @Override
    public String toString() {
        return String.format("Cargo{id=%s, nome='%s', categoria='%s', hierarquia=%s, ativo=%s}",
                id, nome, getCategoriaNome(),
                hierarquia != null ? hierarquia.getDisplayName() : "null", ativo);
    }
}