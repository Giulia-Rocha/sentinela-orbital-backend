package br.com.sentinela.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@Service
@Slf4j
public class SoapClientService extends WebServiceGatewaySupport {

    @Value("${soap.url}")
    private String soapUrl;

    public SoapResultado processarLeitura(Long regiaoId, Double temperatura,
                                           Double umidade, Double iuv) {
        String requestXml = String.format(java.util.Locale.US,
                """
                <sen:processarLeituraRequest xmlns:sen="http://sentinela.com.br/soap">
                    <sen:regiaoId>%d</sen:regiaoId>
                    <sen:temperatura>%.2f</sen:temperatura>
                    <sen:umidade>%.2f</sen:umidade>
                    <sen:iuv>%.2f</sen:iuv>
                </sen:processarLeituraRequest>
                """, regiaoId, temperatura, umidade, iuv);

        try {
            StringResult result = new StringResult();
            getWebServiceTemplate().sendSourceAndReceiveToResult(
                    soapUrl,
                    new StringSource(requestXml),
                    result
            );
            return parseSoapResultado(result.toString());
        } catch (Exception e) {
            log.error("Erro ao chamar SOAP: {}", e.getMessage());
            // fallback: calcula localmente se SOAP estiver fora
            double hri = calcularHriLocal(temperatura, umidade, iuv);
            String nivel = hri >= 8.0 ? "CRITICO" : hri >= 6.0 ? "ALERTA" : "ATENCAO";
            return new SoapResultado(hri, nivel,
                    "Processado localmente (SOAP indisponível): HRI " + String.format("%.1f", hri));
        }
    }

    private SoapResultado parseSoapResultado(String xml) {
        try {
            // remove prefixos de namespace antes de parsear
            String xmlLimpo = xml.replaceAll("<ns\\d+:", "<")
                    .replaceAll("</ns\\d+:", "</")
                    .replaceAll("xmlns:ns\\d+=\"[^\"]*\"", "");

            DocumentBuilder builder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlLimpo)));
            doc.getDocumentElement().normalize();

            String hriStr = doc.getElementsByTagName("hri").item(0).getTextContent().trim();
            String nivel = doc.getElementsByTagName("nivelAlerta").item(0).getTextContent().trim();
            String mensagem = doc.getElementsByTagName("mensagem").item(0).getTextContent().trim();

            double hri = Double.parseDouble(hriStr.replace(",", "."));

            return new SoapResultado(hri, nivel, mensagem);
        } catch (Exception e) {
            log.error("Erro ao parsear resposta SOAP: {}", e.getMessage());
            log.error("XML recebido: {}", xml);
            return new SoapResultado(0.0, "ATENCAO", "Erro ao processar resposta SOAP");
        }
    }

    private String getTagValue(Document doc, String tagName) {
        NodeList list = doc.getElementsByTagNameNS("*", tagName);
        if (list.getLength() == 0) {
            list = doc.getElementsByTagName(tagName);
        }
        if (list.getLength() > 0 && list.item(0) != null) {
            return list.item(0).getTextContent().trim();
        }
        return "";
    }

    private double calcularHriLocal(double temp, double umidade, double iuv) {
        return (temp / 50.0 * 0.4 + umidade / 100.0 * 0.2 + iuv / 12.0 * 0.4) * 10;
    }

    public record SoapResultado(double hri, String nivel, String mensagem) {}
}
