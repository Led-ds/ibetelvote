package com.br.ibetelvote.application.services;

import com.br.ibetelvote.application.eleicao.dto.ConfigurarVagasEleicaoRequest;
import com.br.ibetelvote.application.eleicao.dto.LimiteVotacaoResponse;
import com.br.ibetelvote.application.eleicao.dto.VagasEleicaoResponse;
import com.br.ibetelvote.application.mapper.EleicaoMapper;
import com.br.ibetelvote.domain.entities.Cargo;
import com.br.ibetelvote.domain.entities.Eleicao;
import com.br.ibetelvote.domain.repositories.CargoRepository;
import com.br.ibetelvote.domain.repositories.EleicaoRepository;
import com.br.ibetelvote.infrastructure.repositories.CargoJpaRepository;
import com.br.ibetelvote.infrastructure.repositories.EleicaoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EleicaoConfigService {

    private final EleicaoJpaRepository eleicaoRepository;
    private final CargoJpaRepository cargoRepository;
    private final EleicaoMapper eleicaoMapper;

    // === CONFIGURAÇÃO DE VAGAS ===

    /**
     * Configura vagas por cargo para uma eleição
     */
    public VagasEleicaoResponse configurarVagasPorCargo(UUID eleicaoId, ConfigurarVagasEleicaoRequest request) {
        log.info("Configurando vagas para eleição: {}", eleicaoId);

        // Validações
        if (request.temCargosDuplicados()) {
            throw new IllegalArgumentException("Não é possível configurar o mesmo cargo múltiplas vezes");
        }

        // Buscar eleição
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada: " + eleicaoId));

        // Validar se eleição pode ser configurada
        if (eleicao.isVotacaoAberta()) {
            throw new IllegalStateException("Não é possível alterar configuração com votação em andamento");
        }

        // Validar se todos os cargos existem
        request.getVagasPorCargo().forEach(vaga -> {
            cargoRepository.findById(vaga.getCargoId())
                    .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado: " + vaga.getCargoId()));
        });

        // Aplicar configuração
        eleicaoMapper.aplicarConfigVagas(eleicao, request);

        // Salvar
        Eleicao eleicaoSalva = eleicaoRepository.save(eleicao);

        log.info("Vagas configuradas com sucesso para eleição: {}", eleicaoId);
        return eleicaoMapper.toVagasResponse(eleicaoSalva);
    }

    /**
     * Consulta configuração atual de vagas
     */
    @Transactional(readOnly = true)
    public VagasEleicaoResponse consultarVagasPorCargo(UUID eleicaoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada: " + eleicaoId));

        return eleicaoMapper.toVagasResponse(eleicao);
    }

    /**
     * Consulta limite de votação para um membro em um cargo
     */
    @Transactional(readOnly = true)
    public LimiteVotacaoResponse consultarLimiteVotacao(UUID eleicaoId, UUID membroId, UUID cargoId) {
        // Buscar eleição
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada: " + eleicaoId));

        // Buscar cargo para obter nome
        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new IllegalArgumentException("Cargo não encontrado: " + cargoId));

        // Obter informações de limite
        Eleicao.LimiteVotacaoInfo info = eleicao.getLimiteVotacaoParaMembro(membroId, cargoId);

        return eleicaoMapper.toLimiteVotacaoResponse(info, cargo.getNome());
    }

    // === VALIDAÇÃO E ATIVAÇÃO ===

    /**
     * Valida se eleição está pronta para ser ativada
     */
    @Transactional(readOnly = true)
    public boolean validarConfiguracaoCompleta(UUID eleicaoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada: " + eleicaoId));

        try {
            // Validar candidatos aprovados
            if (!eleicao.temCandidatosAprovados()) {
                log.warn("Eleição {} não tem candidatos aprovados", eleicaoId);
                return false;
            }

            // Validar se todos os cargos com candidatos têm configuração de vagas
            boolean todosCargosConfigure = eleicao.getCargosComCandidatos().stream()
                    .allMatch(cargo -> eleicao.cargoTemConfiguracaoVagas(cargo.getId()));

            if (!todosCargosConfigure) {
                log.warn("Eleição {} possui cargos sem configuração de vagas", eleicaoId);
                return false;
            }

            // Outras validações podem ser adicionadas aqui
            return true;

        } catch (Exception e) {
            log.error("Erro ao validar configuração da eleição {}: {}", eleicaoId, e.getMessage());
            return false;
        }
    }

    /**
     * Ativa eleição após validações
     */
    public void ativarEleicao(UUID eleicaoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada: " + eleicaoId));

        // Validar se pode ser ativada
        if (!validarConfiguracaoCompleta(eleicaoId)) {
            throw new IllegalStateException("Eleição não atende aos requisitos para ativação");
        }

        // Ativar
        eleicao.activate();
        eleicaoRepository.save(eleicao);

        log.info("Eleição {} ativada com sucesso", eleicaoId);
    }

    /**
     * Desativa eleição
     */
    public void desativarEleicao(UUID eleicaoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada: " + eleicaoId));

        eleicao.deactivate();
        eleicaoRepository.save(eleicao);

        log.info("Eleição {} desativada com sucesso", eleicaoId);
    }

    // === MÉTODOS AUXILIARES ===

    /**
     * Verifica se eleição tem configuração mínima
     */
    @Transactional(readOnly = true)
    public boolean temConfiguracaoMinima(UUID eleicaoId) {
        Eleicao eleicao = eleicaoRepository.findById(eleicaoId)
                .orElseThrow(() -> new IllegalArgumentException("Eleição não encontrada: " + eleicaoId));

        return eleicao.temCandidatosAprovados() &&
                !eleicao.getCargosComCandidatos().isEmpty();
    }
}