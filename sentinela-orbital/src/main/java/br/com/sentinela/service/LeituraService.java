package br.com.sentinela.service;

import br.com.sentinela.domain.dto.request.LeituraRequest;
import br.com.sentinela.domain.dto.response.LeituraResponse;
import br.com.sentinela.domain.model.*;
import br.com.sentinela.exception.RegiaoNotFoundException;
import br.com.sentinela.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeituraService {

    private final LeituraClimaticaRepository leituraRepository;
    private final RegiaoRepository regiaoRepository;
    private final AlertaRepository alertaRepository;
    private final SoapClientService soapClientService;

    public LeituraResponse processarLeitura(LeituraRequest request) {
        Regiao regiao = regiaoRepository.findById(request.getRegiaoId())
                .orElseThrow(() -> new RegiaoNotFoundException(request.getRegiaoId()));

        // Chama o serviço SOAP para processar e calcular HRI
        SoapClientService.SoapResultado resultado = soapClientService.processarLeitura(
                request.getRegiaoId(),
                request.getTemperatura(),
                request.getUmidade(),
                request.getIuv()
        );

        // Persiste a leitura localmente
        LeituraClimatica leitura = new LeituraClimatica();
        leitura.setRegiao(regiao);
        leitura.setTemperatura(request.getTemperatura());
        leitura.setUmidade(request.getUmidade());
        leitura.setIuv(request.getIuv());
        leitura.setHri(resultado.hri());
        leitura.setFonte("MANUAL");
        leituraRepository.save(leitura);

        // Se SOAP gerou alerta, persiste localmente também
        if (!resultado.nivel().equals("ATENCAO")) {
            Alerta alerta = resultado.nivel().equals("CRITICO") ? new AlertaCritico()
                    : resultado.nivel().equals("ALERTA")  ? new AlertaAlerta()
                    : new AlertaAtencao();
            alerta.setRegiao(regiao);
            alerta.setLeitura(leitura);
            alerta.setMensagem(resultado.mensagem());
            alertaRepository.save(alerta);
            log.info("Alerta {} gerado para região {}", resultado.nivel(), regiao.getNome());
        }

        return new LeituraResponse(
                leitura.getId(),
                regiao.getId(),
                regiao.getNome(),
                leitura.getTemperatura(),
                leitura.getUmidade(),
                leitura.getIuv(),
                leitura.getHri(),
                leitura.getFonte(),
                leitura.getTimestamp(),
                resultado.nivel(),
                resultado.mensagem()
        );
    }

    public List<LeituraResponse> listarPorRegiao(Long regiaoId) {
        return leituraRepository.findByRegiaoIdOrderByTimestampDesc(regiaoId)
                .stream()
                .map(l -> new LeituraResponse(
                        l.getId(),
                        l.getRegiao().getId(),
                        l.getRegiao().getNome(),
                        l.getTemperatura(),
                        l.getUmidade(),
                        l.getIuv(),
                        l.getHri(),
                        l.getFonte(),
                        l.getTimestamp(),
                        null,
                        null
                ))
                .toList();
    }
}
