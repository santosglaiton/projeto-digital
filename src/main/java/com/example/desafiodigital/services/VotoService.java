package com.example.desafiodigital.services;

import com.example.desafiodigital.domain.Votacao;
import com.example.desafiodigital.dto.ResultadoDto;
import com.example.desafiodigital.domain.Voto;
import com.example.desafiodigital.repositories.VotacaoRepository;
import com.example.desafiodigital.repositories.VotoRepository;
import com.example.desafiodigital.services.integracaoCpfService.IntegracaoCpfService;
import com.example.desafiodigital.services.exception.*;
import com.example.desafiodigital.services.exception.IllegalArgumentException;
import com.example.desafiodigital.services.utils.ValidaCpf;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VotoService {

    private VotoRepository votoRepository;
    private VotacaoService votacaoService;
    private VotacaoRepository votacaoRepository;
    private IntegracaoCpfService integracaoCpfService;

    public VotoService(IntegracaoCpfService integracaoCpfService, VotoRepository votoRepository, VotacaoService votacaoService, VotacaoRepository votacaoRepository){
        this.votoRepository = votoRepository;
        this.votacaoService = votacaoService;
        this.votacaoRepository = votacaoRepository;
        this.integracaoCpfService = integracaoCpfService;
    }

    public void verificaTempoDeVotacao(Integer idVotacao){
        Optional<Votacao> votacao = votacaoRepository.findById(idVotacao);
        if (votacao.isPresent()) {
            LocalDateTime dataLimiteVotacao = votacao.get().getInicioVotacao();
            if (LocalDateTime.now().isAfter(dataLimiteVotacao.plusMinutes(votacao.get().getValidadeVotacao()))) {
                throw new SessaoExpiradaException("Sessao encerrada");
            }
        }else{
            throw new ObjectNotFoundException("Votacao nao encontrada");
        }
    }


    public Voto save(Integer idVotacao, Integer idPauta, Voto voto) {
        var votacao = votacaoService.findById(idVotacao);
        if (votacao.getId() == null) {
            throw new ObjectNotFoundException("Votacao nao encontrada");
        } else {
            voto.setPauta(votacao.getPauta());
            if (voto.getPauta().getId() == null) {
                throw new ObjectNotFoundException("Pauta nao encontrada");
            } else {
                if (voto.getCpf().length() != 11) {
                    throw new CpfInvalidoException("CPF invalido");
                } else {
                    if (integracaoCpfService.validaCpfPodeVotar(voto.getCpf()).equals("UNABLE_TO_VOTE")) {
                        throw new ApiIntegracaoException("CPF nao pode votar");
                    } else {
                        if (voto.getVotoAssociado() == null) {
                            throw new IllegalArgumentException("Voto nao pode ser nulo");
                        } else {
                            votacaoService.findByIdAndPautaId(idVotacao, idPauta);
                            verificaTempoDeVotacao(idVotacao);
                            verificaSeVotoJaExiste(voto);
                            return votoRepository.save(voto);
                        }
                    }
                }
            }
        }
    }
    public void verificaSeVotoJaExiste(Voto voto) {
        Optional<Voto> votoPorCpfEPauta = votoRepository.findByCpfAndPautaId(voto.getCpf(), voto.getPauta().getId());
        if (votoPorCpfEPauta.isPresent()){
            throw new VotoJaExisteException("Voto ja existe para este CPF nesta pauta");
        }
    }

    public ResultadoDto contadorVotos(Integer id) {
        Optional<List<Voto>> votosPorPauta = votoRepository.findByPautaId(id);
        if (votosPorPauta.isPresent()) {
            var pauta = votosPorPauta.get().iterator().next().getPauta();
            Integer total = votosPorPauta.get().size();
            Integer totalSim = (int) votosPorPauta.get().stream().filter(voto -> Boolean.TRUE.equals(voto.getVotoAssociado())).count();
            Integer totalNao = total - totalSim;
            return ResultadoDto.builder().pauta(pauta).totalVotos(total).totalSim(totalSim).totalNao(totalNao).build();
        }
        throw new ObjectNotFoundException("Pauta nao encontrada");
    }

    public ResultadoDto getResultadoVotacao(Integer id){
        var votacaoDto = contadorVotos(id);
        return votacaoDto;
    }

}
