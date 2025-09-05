package com.br.ibetelvote.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Table(name = "eleicoes", indexes = {
        @Index(name = "idx_eleicao_nome", columnList = "nome"),
        @Index(name = "idx_eleicao_ativa", columnList = "ativa"),
        @Index(name = "idx_eleicao_data_inicio", columnList = "data_inicio"),
        @Index(name = "idx_eleicao_data_fim", columnList = "data_fim"),
        @Index(name = "idx_eleicao_status", columnList = "ativa, data_inicio, data_fim")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Eleicao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Nome da eleição é obrigatório")
    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @NotNull(message = "Data de início é obrigatória")
    @Column(name = "data_inicio", nullable = false)
    private LocalDateTime dataInicio;

    @NotNull(message = "Data de fim é obrigatória")
    @Column(name = "data_fim", nullable = false)
    private LocalDateTime dataFim;

    @Builder.Default
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = false;

    @Positive(message = "Total de elegíveis deve ser positivo")
    @Column(name = "total_elegiveis")
    private Integer totalElegiveis;

    @Builder.Default
    @Column(name = "total_votantes", nullable = false)
    private Integer totalVotantes = 0;

    @Builder.Default
    @Column(name = "permite_voto_branco", nullable = false)
    private Boolean permiteVotoBranco = true;

    @Builder.Default
    @Column(name = "permite_voto_nulo", nullable = false)
    private Boolean permiteVotoNulo = true;

    @Builder.Default
    @Column(name = "exibe_resultados_parciais", nullable = false)
    private Boolean exibeResultadosParciais = false;

    @Column(name = "instrucoes_votacao", columnDefinition = "TEXT")
    private String instrucoesVotacao;

    @ElementCollection
    @CollectionTable(name = "eleicao_vagas_cargo",
            joinColumns = @JoinColumn(name = "eleicao_id"))
    @MapKeyColumn(name = "cargo_id")
    @Column(name = "numero_vagas")
    @Builder.Default
    private Map<UUID, Integer> vagasPorCargo = new HashMap<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "eleicao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Candidato> candidatos = new ArrayList<>();

    @OneToMany(mappedBy = "eleicao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Voto> votos = new ArrayList<>();

    /**
     * Ativa a eleição após validações
     */
    public void activate() {
        validarParaAtivacao();
        this.ativa = true;
    }

    /**
     * Desativa a eleição
     */
    public void deactivate() {
        this.ativa = false;
    }

    /**
     * Encerra a eleição permanentemente
     */
    public void encerrar() {
        this.ativa = false;
        this.dataFim = LocalDateTime.now();
    }

    /**
     * Verifica se a eleição está ativa
     */
    public boolean isAtiva() {
        return this.ativa != null && this.ativa;
    }

    /**
     * Verifica se a votação está aberta (ativa + dentro do período)
     */
    public boolean isVotacaoAberta() {
        LocalDateTime now = LocalDateTime.now();
        return isAtiva() &&
                now.isAfter(dataInicio) &&
                now.isBefore(dataFim);
    }

    /**
     * Verifica se a votação já foi encerrada
     */
    public boolean isVotacaoEncerrada() {
        return LocalDateTime.now().isAfter(dataFim);
    }

    /**
     * Verifica se a votação é futura
     */
    public boolean isVotacaoFutura() {
        return LocalDateTime.now().isBefore(dataInicio);
    }

    /**
     * Verifica se tem candidatos aprovados
     */
    public boolean temCandidatosAprovados() {
        return candidatos != null && candidatos.stream()
                .anyMatch(candidato -> candidato.getAprovado() != null && candidato.getAprovado());
    }

    /**
     * Retorna candidatos aprovados
     */
    public List<Candidato> getCandidatosAprovados() {
        if (candidatos == null) return new ArrayList<>();

        return candidatos.stream()
                .filter(candidato -> candidato.getAprovado() != null && candidato.getAprovado())
                .collect(Collectors.toList());
    }

    /**
     * Retorna candidatos aprovados por cargo específico
     */
    public List<Candidato> getCandidatosPorCargo(UUID cargoId) {
        if (candidatos == null || cargoId == null) return new ArrayList<>();

        return candidatos.stream()
                .filter(candidato -> candidato.getAprovado() != null && candidato.getAprovado())
                .filter(candidato -> candidato.getCargoPretendido() != null)
                .filter(candidato -> cargoId.equals(candidato.getCargoPretendido().getId()))
                .collect(Collectors.toList());
    }

    /**
     * Verifica se tem candidatos para um cargo específico
     */
    public boolean temCandidatosParaCargo(UUID cargoId) {
        return !getCandidatosPorCargo(cargoId).isEmpty();
    }

    /**
     * Retorna cargos que têm candidatos aprovados nesta eleição
     */
    public List<Cargo> getCargosComCandidatos() {
        if (candidatos == null) return new ArrayList<>();

        return candidatos.stream()
                .filter(candidato -> candidato.getAprovado() != null && candidato.getAprovado())
                .map(Candidato::getCargoPretendido)
                .filter(cargo -> cargo != null)
                .distinct()
                .collect(Collectors.toList());
    }

    public Integer getLimiteVotosPorCargo(UUID cargoId) {
        return vagasPorCargo.getOrDefault(cargoId, 0);
    }

    /**
     * Verifica se cargo tem configuração de vagas
     */
    public boolean cargoTemConfiguracaoVagas(UUID cargoId) {
        return vagasPorCargo.containsKey(cargoId) && vagasPorCargo.get(cargoId) > 0;
    }

    /**
     * Retorna candidatos que o membro já votou em um cargo específico
     */
    public List<UUID> getCandidatosJaVotadosPeloMembroNoCargo(UUID membroId, UUID cargoId) {
        if (votos == null) return new ArrayList<>();

        return votos.stream()
                .filter(voto -> voto.getMembro().getId().equals(membroId))
                .filter(voto -> voto.getCargoPretendido().getId().equals(cargoId))
                .filter(voto -> voto.getCandidato() != null) // Apenas votos em candidatos
                .map(voto -> voto.getCandidato().getId())
                .collect(Collectors.toList());
    }

    /**
     * Conta quantos votos o membro já deu para um cargo específico
     */
    public int contarVotosDoMembroNoCargo(UUID membroId, UUID cargoId) {
        return getCandidatosJaVotadosPeloMembroNoCargo(membroId, cargoId).size();
    }

    /**
     * Verifica se membro já votou no candidato específico
     */
    public boolean membroJaVotouNoCandidato(UUID membroId, UUID candidatoId) {
        if (votos == null || candidatoId == null) return false;

        return votos.stream()
                .anyMatch(voto -> voto.getMembro().getId().equals(membroId) &&
                        voto.getCandidato() != null &&
                        voto.getCandidato().getId().equals(candidatoId));
    }

    /**
     * Verifica se membro pode votar mais no cargo (não exceder limite de vagas)
     */
    public boolean membroPodeVotarMaisNoCargo(UUID membroId, UUID cargoId) {
        // TODO: Implementar quando tivermos configuração de vagas por cargo
        // Por enquanto, assume limite muito alto para não bloquear
        int votosJaDados = contarVotosDoMembroNoCargo(membroId, cargoId);
        int limiteVagas = 50; // Temporário - será configurável

        return votosJaDados < limiteVagas;
    }

    /**
     * Verifica se membro pode votar no candidato específico
     */
    public void validarVotoDoMembroNoCandidato(UUID membroId, UUID candidatoId, UUID cargoId) {
        if (!isVotacaoAberta()) {
            throw new IllegalStateException("Votação não está aberta");
        }

        if (membroJaVotouNoCandidato(membroId, candidatoId)) {
            throw new IllegalStateException("Membro já votou neste candidato");
        }

        if (!cargoTemConfiguracaoVagas(cargoId)) {
            throw new IllegalStateException("Cargo não possui configuração de vagas para esta eleição");
        }

        if (!membroPodeVotarMaisNoCargo(membroId, cargoId)) {
            int limite = getLimiteVotosPorCargo(cargoId);
            throw new IllegalStateException(
                    String.format("Membro já atingiu o limite de %d votos para este cargo", limite)
            );
        }

        if (!temCandidatosParaCargo(cargoId)) {
            throw new IllegalStateException("Não há candidatos aprovados para este cargo");
        }
    }

    /**
     * Retorna informações de limite para um membro em um cargo
     */
    public LimiteVotacaoInfo getLimiteVotacaoParaMembro(UUID membroId, UUID cargoId) {
        int votosJaDados = contarVotosDoMembroNoCargo(membroId, cargoId);
        Integer limiteVotos = getLimiteVotosPorCargo(cargoId);
        List<UUID> candidatosJaVotados = getCandidatosJaVotadosPeloMembroNoCargo(membroId, cargoId);

        return new LimiteVotacaoInfo(
                cargoId,
                limiteVotos,
                votosJaDados,
                candidatosJaVotados,
                membroPodeVotarMaisNoCargo(membroId, cargoId)
        );
    }

    /**
     * Verifica se pode ser ativada
     */
    public boolean podeSerAtivada() {
        try {
            validarParaAtivacao();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retorna total de candidatos aprovados
     */
    public int getTotalCandidatosAprovados() {
        return getCandidatosAprovados().size();
    }

    /**
     * Retorna total de cargos com candidatos
     */
    public int getTotalCargosComCandidatos() {
        return getCargosComCandidatos().size();
    }

    /**
     * ✅ Configura vagas para múltiplos cargos
     */
    public void configurarVagasMultiplosCargos(Map<UUID, Integer> novasVagas) {
        if (novasVagas != null) {
            // Validar todas as vagas primeiro
            novasVagas.forEach((cargoId, vagas) -> {
                if (vagas == null || vagas < 0) {
                    throw new IllegalArgumentException("Número de vagas deve ser maior ou igual a zero para cargo: " + cargoId);
                }
            });

            // Se todas são válidas, aplicar
            this.vagasPorCargo.clear();
            this.vagasPorCargo.putAll(novasVagas);
        }
    }

    /**
     * Verifica se tem candidatos (independente do status)
     */
    public boolean temCandidatos() {
        return candidatos != null && !candidatos.isEmpty();
    }

    /**
     * Calcula duração da eleição em horas
     */
    public long getDuracaoEmHoras() {
        if (dataInicio == null || dataFim == null) {
            return 0;
        }
        return java.time.Duration.between(dataInicio, dataFim).toHours();
    }

    // Classe helper para retorno de informações
    public static class LimiteVotacaoInfo {
        public final UUID cargoId;
        public final Integer limiteVotos;
        public final Integer votosJaDados;
        public final List<UUID> candidatosJaVotados;
        public final boolean podeVotarMais;

        public LimiteVotacaoInfo(UUID cargoId, Integer limiteVotos, Integer votosJaDados,
                                 List<UUID> candidatosJaVotados, boolean podeVotarMais) {
            this.cargoId = cargoId;
            this.limiteVotos = limiteVotos;
            this.votosJaDados = votosJaDados;
            this.candidatosJaVotados = candidatosJaVotados;
            this.podeVotarMais = podeVotarMais;
        }

        public int getVotosRestantes() {
            return Math.max(0, (limiteVotos != null ? limiteVotos : 0) - (votosJaDados != null ? votosJaDados : 0));
        }
    }

    /**
     * Incrementa o contador de votantes
     */
    public void incrementarVotantes() {
        this.totalVotantes++;
    }

    /**
     * Retorna descrição do status atual
     */
    public String getStatusDescricao() {
        if (isVotacaoFutura()) {
            return "Aguardando início";
        } else if (isVotacaoAberta()) {
            return "Votação em andamento";
        } else if (isVotacaoEncerrada()) {
            return "Encerrada";
        } else if (isAtiva()) {
            return "Ativa (aguardando período)";
        } else {
            return "Inativa";
        }
    }

    /**
     * Calcula percentual de participação
     */
    public double getPercentualParticipacao() {
        if (totalElegiveis == null || totalElegiveis == 0) {
            return 0.0;
        }
        return (totalVotantes.doubleValue() / totalElegiveis.doubleValue()) * 100.0;
    }

    /**
     * Retorna total de votos contabilizados
     */
    public int getTotalVotosContabilizados() {
        return votos != null ? votos.size() : 0;
    }

    /**
     * Valida se a eleição pode ser ativada
     */
    private void validarParaAtivacao() {
        if (!temCandidatosAprovados()) {
            throw new IllegalStateException("Não é possível ativar eleição sem candidatos aprovados");
        }

        if (dataInicio == null || dataFim == null) {
            throw new IllegalStateException("Datas de início e fim são obrigatórias");
        }

        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalStateException("Data de início deve ser anterior à data de fim");
        }

        if (totalElegiveis == null || totalElegiveis <= 0) {
            throw new IllegalStateException("Total de elegíveis deve ser informado e maior que zero");
        }

        if (getCargosComCandidatos().isEmpty()) {
            throw new IllegalStateException("Deve haver pelo menos um cargo com candidatos aprovados");
        }
    }

    /**
     * Atualiza informações básicas da eleição
     */
    public void updateBasicInfo(String nome, String descricao, LocalDateTime dataInicio,
                                LocalDateTime dataFim, String instrucoesVotacao) {
        this.nome = nome;
        this.descricao = descricao;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.instrucoesVotacao = instrucoesVotacao;
    }

    /**
     * Atualiza configurações da eleição
     */
    public void updateConfiguracoes(Boolean permiteVotoBranco, Boolean permiteVotoNulo,
                                    Boolean exibeResultadosParciais, Integer totalElegiveis) {
        this.permiteVotoBranco = permiteVotoBranco;
        this.permiteVotoNulo = permiteVotoNulo;
        this.exibeResultadosParciais = exibeResultadosParciais;
        this.totalElegiveis = totalElegiveis;
    }

    public void validarDados() {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da eleição é obrigatório");
        }

        if (nome.length() > 200) {
            throw new IllegalArgumentException("Nome da eleição deve ter no máximo 200 caracteres");
        }

        if (descricao != null && descricao.length() > 1000) {
            throw new IllegalArgumentException("Descrição deve ter no máximo 1000 caracteres");
        }

        if (instrucoesVotacao != null && instrucoesVotacao.length() > 2000) {
            throw new IllegalArgumentException("Instruções de votação deve ter no máximo 2000 caracteres");
        }

        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Data de início deve ser anterior à data de fim");
        }

        if (totalElegiveis != null && totalElegiveis <= 0) {
            throw new IllegalArgumentException("Total de elegíveis deve ser positivo");
        }
    }

    public void normalizarNome() {
        if (this.nome != null) {
            // Remove espaços no início e fim, e substitui múltiplos espaços por um único espaço
            this.nome = this.nome.trim().replaceAll("\\s+", " ");
        }
    }

    @Override
    public String toString() {
        return String.format("Eleicao{id=%s, nome='%s', ativa=%s, status='%s'}",
                id, nome, ativa, getStatusDescricao());
    }
}