package br.com.sentinela.service;

import br.com.sentinela.domain.dto.response.PrevisaoDiaResponse;
import br.com.sentinela.domain.dto.response.PrevisaoResponse;
import br.com.sentinela.domain.model.Regiao;
import br.com.sentinela.exception.RegiaoNotFoundException;
import br.com.sentinela.repository.RegiaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.springframework.http.HttpHeaders;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CptecService {

    private final RestTemplate restTemplate;
    private final RegiaoRepository regiaoRepository;

    private static final String BASE = "http://servicos.cptec.inpe.br/XML";

    // Mapa simplificado de coordenadas para código CPTEC
    // Em produção isso seria uma tabela no banco
    private static final java.util.Map<String, Integer> CIDADE_CODIGO = java.util.Map.of(
            "São Paulo", 244,
            "Campinas",  690,
            "default",   244
    );

    public PrevisaoResponse buscarPrevisaoPorRegiao(Long regiaoId) {
        Regiao regiao = regiaoRepository.findById(regiaoId)
                .orElseThrow(() -> new RegiaoNotFoundException(regiaoId));

        int codigo = resolverCodigoCidade(regiao.getNome());
        return buscarPrevisao(codigo, regiao.getNome());
    }

//    private PrevisaoResponse buscarPrevisao(int codigoCidade, String nomeRegiao) {
//        try {
//            String url = BASE + "/cidade/" + codigoCidade + "/previsao.xml";
//            String xml = restTemplate.getForObject(url, String.class);
//            return parsePrevisao(xml, nomeRegiao);
//        } catch (Exception e) {
//            log.warn("CPTEC indisponível, usando dados mockados: {}", e.getMessage());
//            return mockPrevisao(nomeRegiao);
//        }
//    }
//
//private PrevisaoResponse buscarPrevisao(int codigoCidade, String nomeRegiao) {
//    try {
//        // tenta as duas variações de URL do CPTEC
//        String[] urls = {
//                "http://servicos.cptec.inpe.br/XML/cidade/" + codigoCidade + "/previsao.xml",
//                "http://servicos.cptec.inpe.br/XML/cidade/" + codigoCidade + "/7dias/previsao.xml"
//        };
//
//        for (String url : urls) {
//            try {
//                HttpHeaders headers = new HttpHeaders();
//                headers.set("User-Agent", "Mozilla/5.0");
//                headers.set("Accept", "application/xml, text/xml, */*");
//                HttpEntity<String> entity = new HttpEntity<>(headers);
//
//                ResponseEntity<String> response = restTemplate.exchange(
//                        url, HttpMethod.GET, entity, String.class);
//
//                String xml = response.getBody();
//                log.info("URL {} retornou: {}", url, xml != null ? xml.substring(0, Math.min(200, xml.length())) : "null");
//
//                if (xml != null && !xml.isBlank()) {
//                    return parsePrevisao(xml, nomeRegiao);
//                }
//            } catch (Exception ex) {
//                log.warn("Falha na URL {}: {}", url, ex.getMessage());
//            }
//        }
//
//        log.warn("Todas as URLs do CPTEC falharam, usando mock");
//        return mockPrevisao(nomeRegiao);
//
//    } catch (Exception e) {
//        log.warn("CPTEC indisponível: {}", e.getMessage());
//        return mockPrevisao(nomeRegiao);
//    }
//}

//    private PrevisaoResponse buscarPrevisao(int codigoCidade, String nomeRegiao) {
//        try {
//            String url = "http://servicos.cptec.inpe.br/XML/cidade/" + codigoCidade + "/previsao.xml";
//
//            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
//                    .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
//                    .build();
//
//            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
//                    .uri(java.net.URI.create(url))
//                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
//                    .header("Accept", "text/xml, application/xml, */*")
//                    .GET()
//                    .build();
//
//            java.net.http.HttpResponse<String> response = client.send(
//                    request, java.net.http.HttpResponse.BodyHandlers.ofString());
//
//            String xml = response.body();
//            log.info("HttpClient status: {} body: {}", response.statusCode(),
//                    xml != null ? xml.substring(0, Math.min(300, xml.length())) : "null");
//
//            if (xml != null && !xml.isBlank()) {
//                return parsePrevisao(xml, nomeRegiao);
//            }
//
//            return mockPrevisao(nomeRegiao);
//
//        } catch (Exception e) {
//            log.warn("CPTEC indisponível: {}", e.getMessage());
//            return mockPrevisao(nomeRegiao);
//        }
//    }

    private PrevisaoResponse buscarPrevisao(int codigoCidade, String nomeRegiao) {
        try {
            String url = "http://servicos.cptec.inpe.br/XML/cidade/" + codigoCidade + "/previsao.xml";

            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                    .build();

            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept", "text/xml, application/xml, */*")
                    .GET()
                    .build();

            java.net.http.HttpResponse<String> response = client.send(
                    request, java.net.http.HttpResponse.BodyHandlers.ofString());

            String xml = response.body();

            if (xml != null && !xml.isBlank()) {
                return parsePrevisao(xml, nomeRegiao);
            }

            log.warn("CPTEC retornou resposta vazia para cidade {}", codigoCidade);
            return mockPrevisao(nomeRegiao);

        } catch (Exception e) {
            log.warn("CPTEC indisponível, usando dados mockados: {}", e.getMessage());
            return mockPrevisao(nomeRegiao);
        }
    }

//    private PrevisaoResponse parsePrevisao(String xml, String nomeRegiao) {
//        try {
//            DocumentBuilder builder = DocumentBuilderFactory
//                    .newInstance().newDocumentBuilder();
//            Document doc = builder.parse(new InputSource(new StringReader(xml)));
//
//            NodeList nodes = doc.getElementsByTagName("previsao");
//            List<PrevisaoDiaResponse> previsoes = new ArrayList<>();
//            double maxTempGlobal = 0.0;
//
//            for (int i = 0; i < nodes.getLength(); i++) {
//                Element element = (Element) nodes.item(i);
//                double max = Double.parseDouble(element.getElementsByTagName("maxima").item(0).getTextContent());
//                double min = Double.parseDouble(element.getElementsByTagName("minima").item(0).getTextContent());
//                double iuv = Double.parseDouble(element.getElementsByTagName("iuv").item(0).getTextContent());
//                String data = element.getElementsByTagName("dia").item(0).getTextContent();
//
//                previsoes.add(new PrevisaoDiaResponse(data, max, min, iuv));
//                if (max > maxTempGlobal) maxTempGlobal = max;
//            }
//
//            String risco = maxTempGlobal >= 40 ? "EXTREMO"
//                    : maxTempGlobal >= 35 ? "ALTO"
//                    : maxTempGlobal >= 30 ? "MODERADO" : "BAIXO";
//
//            return PrevisaoResponse.builder()
//                    .nomeRegiao(nomeRegiao)
//                    .fonte("CPTEC/INPE")
//                    .riscoCalor(risco)
//                    .previsoes(previsoes)
//                    .build();
//        } catch (Exception e) {
//            log.error("Erro ao parsear XML do CPTEC: {}", e.getMessage());
//            return mockPrevisao(nomeRegiao);
//        }
//    }

    private PrevisaoResponse parsePrevisao(String xml, String nomeRegiao) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList previsoes = doc.getElementsByTagName("previsao");
            List<PrevisaoDiaResponse> lista = new ArrayList<>();
            double maxGeral = 0;

            for (int i = 0; i < previsoes.getLength(); i++) {
                Element el = (Element) previsoes.item(i);
                String dia = el.getElementsByTagName("dia").item(0).getTextContent();
                double tempMax = Double.parseDouble(el.getElementsByTagName("maxima").item(0).getTextContent());
                double tempMin = Double.parseDouble(el.getElementsByTagName("minima").item(0).getTextContent());
                double iuv = Double.parseDouble(el.getElementsByTagName("iuv").item(0).getTextContent());
                lista.add(new PrevisaoDiaResponse(dia, tempMax, tempMin, iuv));
                if (tempMax > maxGeral) maxGeral = tempMax;
            }

            String risco = maxGeral >= 40 ? "EXTREMO"
                    : maxGeral >= 35 ? "ALTO"
                    : maxGeral >= 30 ? "MODERADO" : "BAIXO";

            return PrevisaoResponse.builder()
                    .nomeRegiao(nomeRegiao)
                    .fonte("CPTEC/INPE")
                    .riscoCalor(risco)
                    .previsoes(lista)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao parsear XML do CPTEC: {}", e.getMessage());
            return mockPrevisao(nomeRegiao);
        }
    }

    private PrevisaoResponse mockPrevisao(String nomeRegiao) {
        List<PrevisaoDiaResponse> lista = List.of(
                new PrevisaoDiaResponse("2026-06-05", 35.0, 22.0, 6.0),
                new PrevisaoDiaResponse("2026-06-06", 37.0, 23.0, 7.0),
                new PrevisaoDiaResponse("2026-06-07", 38.0, 24.0, 8.0)
        );
        return PrevisaoResponse.builder()
                .nomeRegiao(nomeRegiao)
                .fonte("MOCK")
                .riscoCalor("ALTO")
                .previsoes(lista)
                .build();
    }

    private int resolverCodigoCidade(String nomeRegiao) {
        return CIDADE_CODIGO.entrySet().stream()
                .filter(e -> nomeRegiao.toLowerCase()
                        .contains(e.getKey().toLowerCase()))
                .findFirst()
                .map(java.util.Map.Entry::getValue)
                .orElse(CIDADE_CODIGO.get("default"));
    }
}
