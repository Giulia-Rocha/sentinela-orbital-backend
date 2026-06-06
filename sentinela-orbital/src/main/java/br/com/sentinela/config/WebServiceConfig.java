package br.com.sentinela.config;

import br.com.sentinela.service.SoapClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class WebServiceConfig {

    @Value("${soap.url}")
    private String soapUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public String soapUrl() {
        return soapUrl;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        return new WebServiceTemplate();
    }

    @Bean
    public SoapClientService soapClientService() {
        SoapClientService client = new SoapClientService();
        client.setWebServiceTemplate(webServiceTemplate());
        return client;
    }
}
