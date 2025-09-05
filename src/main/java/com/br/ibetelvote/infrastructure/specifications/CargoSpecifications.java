package com.br.ibetelvote.infrastructure.specifications;

import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.enums.HierarquiaCargo;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class CargoSpecifications {

    public static Specification<Cargo> comNome(String nome) {
        return (root, query, cb) -> {
            if (nome == null || nome.trim().isEmpty()) {
                return null;
            }
            return cb.like(cb.upper(root.get("nome")),
                    "%" + nome.trim().toUpperCase() + "%");
        };
    }

    public static Specification<Cargo> daCategoria(UUID categoriaId) {
        return (root, query, cb) ->
                categoriaId == null ? null :
                        cb.equal(root.get("categoria").get("id"), categoriaId);
    }

    public static Specification<Cargo> comHierarquia(HierarquiaCargo hierarquia) {
        return (root, query, cb) ->
                hierarquia == null ? null :
                        cb.equal(root.get("hierarquia"), hierarquia);
    }

    public static Specification<Cargo> ativo(Boolean ativo) {
        return (root, query, cb) ->
                ativo == null ? null :
                        cb.equal(root.get("ativo"), ativo);
    }

    public static Specification<Cargo> disponivelParaEleicao(Boolean disponivel) {
        return (root, query, cb) ->
                disponivel == null ? null :
                        cb.equal(root.get("disponivelEleicao"), disponivel);
    }

    /**
     * Busca cargos que podem eleger para um cargo específico
     * Verifica se o nome do cargo está na lista de elegibilidade
     */
    public static Specification<Cargo> podeElegerPara(String nomeCargo) {
        return (root, query, cb) -> {
            if (nomeCargo == null || nomeCargo.trim().isEmpty()) {
                return null;
            }
            // Busca em elegibilidade usando LIKE para encontrar o nome
            return cb.like(
                    cb.function("array_to_string", String.class,
                            root.get("elegibilidade"), cb.literal(",")),
                    "%" + nomeCargo.trim() + "%"
            );
        };
    }

    /**
     * Busca cargos que podem eleger baseado na hierarquia
     */
    public static Specification<Cargo> podeElegerParaHierarquia(HierarquiaCargo hierarquia) {
        return (root, query, cb) -> {
            if (hierarquia == null) {
                return null;
            }
            // Busca por hierarquia na elegibilidade
            return cb.or(
                    cb.like(
                            cb.function("array_to_string", String.class,
                                    root.get("elegibilidade"), cb.literal(",")),
                            "%" + hierarquia.name() + "%"
                    ),
                    cb.like(
                            cb.function("array_to_string", String.class,
                                    root.get("elegibilidade"), cb.literal(",")),
                            "%" + hierarquia.getDisplayName() + "%"
                    )
            );
        };
    }

    /**
     * Verifica elegibilidade entre dois cargos específicos
     */
    public static Specification<Cargo> elegibilidadeEntreCargos(UUID cargoOrigemId, UUID cargoDestinoId) {
        return (root, query, cb) -> {
            if (cargoOrigemId == null || cargoDestinoId == null) {
                return null;
            }

            // Subquery para buscar o cargo origem
            var subquery = query.subquery(String.class);
            var cargoOrigem = subquery.from(Cargo.class);
            subquery.select(cargoOrigem.get("nome"))
                    .where(cb.equal(cargoOrigem.get("id"), cargoOrigemId));

            // Verifica se o cargo destino tem o cargo origem na elegibilidade
            return cb.and(
                    cb.equal(root.get("id"), cargoDestinoId),
                    cb.like(
                            cb.function("array_to_string", String.class,
                                    root.get("elegibilidade"), cb.literal(",")),
                            cb.concat("%", cb.concat(subquery, "%"))
                    )
            );
        };
    }

    /**
     * Busca cargos elegíveis para um nível específico de membro
     */
    public static Specification<Cargo> elegiveisParaNivelMembro(String nivelMembro) {
        return (root, query, cb) -> {
            if (nivelMembro == null || nivelMembro.trim().isEmpty()) {
                return null;
            }
            return cb.like(
                    cb.function("array_to_string", String.class,
                            root.get("elegibilidade"), cb.literal(",")),
                    "%" + nivelMembro.trim() + "%"
            );
        };
    }

    public static Specification<Cargo> ordenadoPorPrecedencia() {
        return (root, query, cb) -> {
            query.orderBy(
                    cb.asc(root.get("categoria").get("ordemExibicao")),
                    cb.asc(root.get("ordemPrecedencia")),
                    cb.asc(root.get("nome"))
            );
            return null;
        };
    }

    public static Specification<Cargo> ordenadoPorNome() {
        return (root, query, cb) -> {
            query.orderBy(cb.asc(root.get("nome")));
            return null;
        };
    }

    public static Specification<Cargo> ordenadoPorHierarquia() {
        return (root, query, cb) -> {
            query.orderBy(
                    cb.asc(root.get("hierarquia")),
                    cb.asc(root.get("nome"))
            );
            return null;
        };
    }

    /**
     * Busca cargos com nome específico (para validação de unicidade)
     */
    public static Specification<Cargo> comNomeExato(String nome) {
        return (root, query, cb) -> {
            if (nome == null || nome.trim().isEmpty()) {
                return null;
            }
            return cb.equal(cb.upper(root.get("nome")), nome.trim().toUpperCase());
        };
    }

    /**
     * Busca cargos com nome específico excluindo um ID (para update)
     */
    public static Specification<Cargo> comNomeExatoExcluindoId(String nome, UUID cargoId) {
        return (root, query, cb) -> {
            if (nome == null || nome.trim().isEmpty() || cargoId == null) {
                return null;
            }
            return cb.and(
                    cb.equal(cb.upper(root.get("nome")), nome.trim().toUpperCase()),
                    cb.notEqual(root.get("id"), cargoId)
            );
        };
    }

    /**
     * Busca cargos com ordem de precedência específica na categoria
     */
    public static Specification<Cargo> comOrdemPrecedencia(UUID categoriaId, Integer ordem) {
        return (root, query, cb) -> {
            if (categoriaId == null || ordem == null) {
                return null;
            }
            return cb.and(
                    cb.equal(root.get("categoria").get("id"), categoriaId),
                    cb.equal(root.get("ordemPrecedencia"), ordem)
            );
        };
    }

    /**
     * Busca cargos com informações incompletas
     */
    public static Specification<Cargo> comInformacoesIncompletas() {
        return (root, query, cb) ->
                cb.or(
                        cb.isNull(root.get("nome")),
                        cb.equal(root.get("nome"), ""),
                        cb.isNull(root.get("descricao")),
                        cb.equal(root.get("descricao"), ""),
                        cb.isNull(root.get("categoria")),
                        cb.isNull(root.get("hierarquia"))
                );
    }

    /**
     * Busca cargos que possuem candidatos
     */
    public static Specification<Cargo> comCandidatos() {
        return (root, query, cb) ->
                cb.isNotEmpty(root.get("candidatos"));
    }

    /**
     * Busca cargos sem candidatos
     */
    public static Specification<Cargo> semCandidatos() {
        return (root, query, cb) ->
                cb.isEmpty(root.get("candidatos"));
    }

    /**
     * Busca cargos criados em um período
     */
    public static Specification<Cargo> criadosEntre(java.time.LocalDateTime inicio, java.time.LocalDateTime fim) {
        return (root, query, cb) -> {
            if (inicio == null && fim == null) {
                return null;
            }
            if (inicio != null && fim != null) {
                return cb.between(root.get("createdAt"), inicio, fim);
            }
            if (inicio != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), inicio);
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), fim);
        };
    }
}