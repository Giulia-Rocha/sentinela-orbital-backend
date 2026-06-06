package br.com.sentinelasoap.ws;

import br.com.sentinelasoap.domain.model.*;
import br.com.sentinelasoap.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Endpoint
@RequiredArgsConstructor
@Slf4j
public class AlertaWebService {

    private static final String NS = "http://sentinela.com.br/soap";

    private final AlertaRepository alertaRepository;
    private final LeituraClimaticaRepository leituraRepository;
    private final RegiaoRepository regiaoRepository;

    // ─── Consultar alertas ────────────────────────────────────────────────────

    @PayloadRoot(namespace = NS, localPart = "consultarAlertasRequest")
    @ResponsePayload
    public ConsultarAlertasResponse consultarAlertas(
            @RequestPayload ConsultarAlertasRequest request) {

        log.info("SOAP consultarAlertas regiaoId={}", request.getRegiaoId());

        List<Alerta> alertas = alertaRepository
                .findByRegiaoIdAndAtivoTrue(request.getRegiaoId());

        ConsultarAlertasResponse response = new ConsultarAlertasResponse();
        for (Alerta a : alertas) {
            AlertaDto dto = new AlertaDto();
            dto.setId(a.getId());
            dto.setNivel(a.getNivel());
            dto.setMensagem(a.getMensagem());
            dto.setAtivo(a.getAtivo());
            dto.setNomeRegiao(a.getRegiao().getNome());
            response.getAlertas().add(dto);
        }
        return response;
    }

    // ─── Processar leitura ────────────────────────────────────────────────────

    @PayloadRoot(namespace = NS, localPart = "processarLeituraRequest")
    @ResponsePayload
    public ProcessarLeituraResponse processarLeitura(
            @RequestPayload ProcessarLeituraRequest request) {

        log.info("SOAP processarLeitura regiaoId={} temp={} umid={} iuv={}",
                request.getRegiaoId(), request.getTemperatura(),
                request.getUmidade(), request.getIuv());

        double hri = calcularHri(request.getTemperatura(),
                request.getUmidade(), request.getIuv());

        String nivel = hri >= 8.0 ? "CRITICO" : hri >= 6.0 ? "ALERTA" : "ATENCAO";
        String mensagem = gerarMensagem(nivel, request.getTemperatura(), hri);

        Regiao regiao = regiaoRepository.findById(request.getRegiaoId())
                .orElseThrow(() -> new RuntimeException(
                        "Região não encontrada: " + request.getRegiaoId()));

        LeituraClimatica leitura = new LeituraClimatica();
        leitura.setRegiao(regiao);
        leitura.setTemperatura(request.getTemperatura());
        leitura.setUmidade(request.getUmidade());
        leitura.setIuv(request.getIuv());
        leitura.setHri(hri);
        leitura.setFonte("SOAP");
        leituraRepository.save(leitura);

        if (!nivel.equals("ATENCAO")) {
            Alerta alerta = nivel.equals("CRITICO") ? new AlertaCritico()
                    : nivel.equals("ALERTA")  ? new AlertaAlerta()
                    : new AlertaAtencao();
            alerta.setRegiao(regiao);
            alerta.setLeitura(leitura);
            alerta.setMensagem(mensagem);
            alertaRepository.save(alerta);
            log.info("Alerta {} persistido para região {}", nivel, regiao.getNome());
        }

        ProcessarLeituraResponse response = new ProcessarLeituraResponse();
        response.setHri(hri);
        response.setNivelAlerta(nivel);
        response.setMensagem(mensagem);
        return response;
    }

    // ─── Cálculo HRI ─────────────────────────────────────────────────────────

    private double calcularHri(double temp, double umidade, double iuv) {
        return (temp / 50.0 * 0.4 + umidade / 100.0 * 0.2 + iuv / 12.0 * 0.4) * 10;
    }

    private String gerarMensagem(String nivel, double temp, double hri) {
        return switch (nivel) {
            case "CRITICO" -> String.format(
                    "ALERTA CRÍTICO: Temperatura %.1f°C, HRI %.1f. Risco extremo de onda de calor.",
                    temp, hri);
            case "ALERTA" -> String.format(
                    "ALERTA: Temperatura %.1f°C, HRI %.1f. Condições de calor intenso.",
                    temp, hri);
            default -> String.format(
                    "ATENÇÃO: Temperatura %.1f°C, HRI %.1f. Monitoramento ativo.",
                    temp, hri);
        };
    }

    // ─── Classes internas XML ─────────────────────────────────────────────────

    @XmlRootElement(name = "consultarAlertasRequest", namespace = NS)
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ConsultarAlertasRequest {

        @XmlElement(name = "regiaoId", namespace = NS)
        private Long regiaoId;

        public Long getRegiaoId() { return regiaoId; }
        public void setRegiaoId(Long regiaoId) { this.regiaoId = regiaoId; }
    }

    @XmlRootElement(name = "consultarAlertasResponse", namespace = NS)
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ConsultarAlertasResponse {

        @XmlElement(name = "alertas")
        private List<AlertaDto> alertas = new ArrayList<>();

        public List<AlertaDto> getAlertas() { return alertas; }
    }

    @XmlRootElement(name = "processarLeituraRequest", namespace = NS)
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProcessarLeituraRequest {

        @XmlElement(name = "regiaoId", namespace = NS)
        private Long regiaoId;

        @XmlElement(name = "temperatura", namespace = NS)
        private Double temperatura;

        @XmlElement(name = "umidade", namespace = NS)
        private Double umidade;

        @XmlElement(name = "iuv", namespace = NS)
        private Double iuv;

        public Long getRegiaoId() { return regiaoId; }
        public void setRegiaoId(Long r) { this.regiaoId = r; }
        public Double getTemperatura() { return temperatura; }
        public void setTemperatura(Double t) { this.temperatura = t; }
        public Double getUmidade() { return umidade; }
        public void setUmidade(Double u) { this.umidade = u; }
        public Double getIuv() { return iuv; }
        public void setIuv(Double i) { this.iuv = i; }
    }

    @XmlRootElement(name = "processarLeituraResponse", namespace = NS)
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ProcessarLeituraResponse {

        @XmlElement(name = "hri", namespace = NS)
        private Double hri;

        @XmlElement(name = "nivelAlerta", namespace = NS)
        private String nivelAlerta;

        @XmlElement(name = "mensagem", namespace = NS)
        private String mensagem;

        public Double getHri() { return hri; }
        public void setHri(Double h) { this.hri = h; }
        public String getNivelAlerta() { return nivelAlerta; }
        public void setNivelAlerta(String n) { this.nivelAlerta = n; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String m) { this.mensagem = m; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AlertaDto {

        @XmlElement(name = "id")
        private Long id;

        @XmlElement(name = "nivel")
        private String nivel;

        @XmlElement(name = "mensagem")
        private String mensagem;

        @XmlElement(name = "ativo")
        private Boolean ativo;

        @XmlElement(name = "nomeRegiao")
        private String nomeRegiao;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNivel() { return nivel; }
        public void setNivel(String nivel) { this.nivel = nivel; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }
        public Boolean getAtivo() { return ativo; }
        public void setAtivo(Boolean ativo) { this.ativo = ativo; }
        public String getNomeRegiao() { return nomeRegiao; }
        public void setNomeRegiao(String nomeRegiao) { this.nomeRegiao = nomeRegiao; }
    }
}