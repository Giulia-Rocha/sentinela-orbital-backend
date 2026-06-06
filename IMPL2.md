# Sentinela Orbital — Implementação Completa (REST + SOAP)

O que está neste arquivo é o complemento do IMPLEMENTACAO.md.
Cobre tudo que faltava para os dois projetos ficarem 100% funcionais.

---

## PARTE 1 — sentinela-api (REST) — o que falta

### Novos arquivos necessários

```
domain/dto/request/LeituraRequest.java
domain/dto/response/LeituraResponse.java
domain/dto/response/PrevisaoResponse.java
service/LeituraService.java
service/SoapClientService.java
controller/LeituraController.java
controller/PrevisaoController.java
```

---

### domain/dto/request/LeituraRequest.java

```java
package br.com.sentinela.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeituraRequest {

    @NotNull
    private Long regiaoId;

    @NotNull
    private Double temperatura;

    @NotNull
    private Double umidade;

    @NotNull
    private Double iuv;
}
```

---

### domain/dto/response/LeituraResponse.java

```java
package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LeituraResponse {
    private Long id;
    private Long regiaoId;
    private String nomeRegiao;
    private Double temperatura;
    private Double umidade;
    private Double iuv;
    private Double hri;
    private String fonte;
    private LocalDateTime timestamp;
    private String nivelAlerta;
    private String mensagemAlerta;
}
```

---

### domain/dto/response/PrevisaoResponse.java

```java
package br.com.sentinela.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrevisaoResponse {
    private String nomeRegiao;
    private Double tempMax;
    private Double tempMin;
    private Double iuv;
    private String fonte;
    private String riscoCalor;
}
```

---

### service/SoapClientService.java

Este serviço é o cliente SOAP — a API REST usa ele para chamar o projeto sentinela-soap.

```java
package br.com.sentinela.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

@Service
@Slf4j
public class SoapClientService extends WebServiceGatewaySupport {

    @Value("${soap.url}")
    private String soapUrl;

    public SoapResultado processarLeitura(Long regiaoId, Double temperatura,
                                           Double umidade, Double iuv) {
        String requestXml = """
                <sen:processarLeituraRequest xmlns:sen="http://sentinela.com.br/soap">
                    <sen:regiaoId>%d</sen:regiaoId>
                    <sen:temperatura>%.2f</sen:temperatura>
                    <sen:umidade>%.2f</sen:umidade>
                    <sen:iuv>%.2f</sen:iuv>
                </sen:processarLeituraRequest>
                """.formatted(regiaoId, temperatura, umidade, iuv);

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
            double hri = Double.parseDouble(
                    extractTag(xml, "hri"));
            String nivel = extractTag(xml, "nivelAlerta");
            String mensagem = extractTag(xml, "mensagem");
            return new SoapResultado(hri, nivel, mensagem);
        } catch (Exception e) {
            log.error("Erro ao parsear resposta SOAP: {}", e.getMessage());
            return new SoapResultado(0.0, "ATENCAO", "Erro ao processar resposta SOAP");
        }
    }

    private String extractTag(String xml, String tag) {
        int start = xml.indexOf("<" + tag + ">") + tag.length() + 2;
        int end = xml.indexOf("</" + tag + ">");
        if (start < 0 || end < 0) return "";
        return xml.substring(start, end).trim();
    }

    private double calcularHriLocal(double temp, double umidade, double iuv) {
        return (temp / 50.0 * 0.4 + umidade / 100.0 * 0.2 + iuv / 12.0 * 0.4) * 10;
    }

    public record SoapResultado(double hri, String nivel, String mensagem) {}
}
```

---

### config/WebServiceConfig.java — versão atualizada

Adiciona a configuração do `WebServiceTemplate` necessária para o `SoapClientService`:

```java
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
```

> Adicione a dependência no pom.xml da API REST:
> ```xml
> <dependency>
>     <groupId>org.springframework.ws</groupId>
>     <artifactId>spring-ws-core</artifactId>
> </dependency>
> ```

---

### service/LeituraService.java

```java
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
            Alerta alerta = resultado.nivel().equals("CRITICO")
                    ? new AlertaCritico() : new AlertaAtencao();
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
```

---

### controller/LeituraController.java

```java
package br.com.sentinela.controller;

import br.com.sentinela.domain.dto.request.LeituraRequest;
import br.com.sentinela.domain.dto.response.LeituraResponse;
import br.com.sentinela.service.LeituraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leituras")
@RequiredArgsConstructor
public class LeituraController {

    private final LeituraService leituraService;

    @PostMapping
    public ResponseEntity<LeituraResponse> processar(@Valid @RequestBody LeituraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leituraService.processarLeitura(request));
    }

    @GetMapping("/regiao/{regiaoId}")
    public ResponseEntity<List<LeituraResponse>> listarPorRegiao(@PathVariable Long regiaoId) {
        return ResponseEntity.ok(leituraService.listarPorRegiao(regiaoId));
    }
}
```

---

### service/CptecService.java — versão corrigida e completa

```java
package br.com.sentinela.service;

import br.com.sentinela.domain.dto.response.PrevisaoResponse;
import br.com.sentinela.domain.model.Regiao;
import br.com.sentinela.exception.RegiaoNotFoundException;
import br.com.sentinela.repository.RegiaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

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

    private PrevisaoResponse buscarPrevisao(int codigoCidade, String nomeRegiao) {
        try {
            String url = BASE + "/cidade/" + codigoCidade + "/previsao.xml";
            String xml = restTemplate.getForObject(url, String.class);
            return parsePrevisao(xml, nomeRegiao);
        } catch (Exception e) {
            log.warn("CPTEC indisponível, usando dados mockados: {}", e.getMessage());
            return mockPrevisao(nomeRegiao);
        }
    }

    private PrevisaoResponse parsePrevisao(String xml, String nomeRegiao) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory
                    .newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            double tempMax = Double.parseDouble(
                    doc.getElementsByTagName("maxima").item(0).getTextContent());
            double tempMin = Double.parseDouble(
                    doc.getElementsByTagName("minima").item(0).getTextContent());
            double iuv = Double.parseDouble(
                    doc.getElementsByTagName("iuv").item(0).getTextContent());

            String risco = tempMax >= 40 ? "EXTREMO"
                    : tempMax >= 35 ? "ALTO"
                    : tempMax >= 30 ? "MODERADO" : "BAIXO";

            return new PrevisaoResponse(nomeRegiao, tempMax, tempMin, iuv, "CPTEC/INPE", risco);
        } catch (Exception e) {
            log.error("Erro ao parsear XML do CPTEC: {}", e.getMessage());
            return mockPrevisao(nomeRegiao);
        }
    }

    private PrevisaoResponse mockPrevisao(String nomeRegiao) {
        return new PrevisaoResponse(nomeRegiao, 35.0, 22.0, 6.0, "MOCK", "ALTO");
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
```

---

### controller/PrevisaoController.java

```java
package br.com.sentinela.controller;

import br.com.sentinela.domain.dto.response.PrevisaoResponse;
import br.com.sentinela.service.CptecService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/previsao")
@RequiredArgsConstructor
public class PrevisaoController {

    private final CptecService cptecService;

    @GetMapping("/regiao/{regiaoId}")
    public ResponseEntity<PrevisaoResponse> buscarPrevisao(@PathVariable Long regiaoId) {
        return ResponseEntity.ok(cptecService.buscarPrevisaoPorRegiao(regiaoId));
    }
}
```

---

### exception/RegiaoNotFoundException.java — versão corrigida

Adiciona um construtor de string genérico para uso em outros serviços:

```java
package br.com.sentinela.exception;

public class RegiaoNotFoundException extends RuntimeException {

    public RegiaoNotFoundException(Long id) {
        super("Região não encontrada: " + id);
    }

    public RegiaoNotFoundException(String mensagem) {
        super(mensagem);
    }
}
```

---

## PARTE 2 — sentinela-soap — implementação completa

### Estrutura de pastas

```
sentinela-soap/
└── src/main/java/br/com/sentinela/soap/
    ├── SentinelaSoapApplication.java
    ├── config/
    │   └── WebServiceConfig.java
    ├── domain/
    │   └── model/
    │       ├── Alerta.java
    │       ├── AlertaAtencao.java
    │       ├── AlertaCritico.java
    │       ├── LeituraClimatica.java
    │       └── Regiao.java
    ├── repository/
    │   ├── AlertaRepository.java
    │   ├── LeituraClimaticaRepository.java
    │   └── RegiaoRepository.java
    └── ws/
        └── AlertaWebService.java
```

---

### pom.xml completo do sentinela-soap

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
    </parent>

    <groupId>br.com.sentinela</groupId>
    <artifactId>sentinela-soap</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>sentinela-soap</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web-services</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>wsdl4j</groupId>
            <artifactId>wsdl4j</artifactId>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### application.yml do sentinela-soap

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sentinela
    username: sentinela
    password: sentinela123
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: false

server:
  port: 8081
```

---

### src/main/resources/alerta.xsd

```xml
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://sentinela.com.br/soap"
           xmlns:tns="http://sentinela.com.br/soap"
           elementFormDefault="qualified">

    <xs:element name="consultarAlertasRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="regiaoId" type="xs:long"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="consultarAlertasResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="alertas" type="tns:AlertaDto"
                            maxOccurs="unbounded" minOccurs="0"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="processarLeituraRequest">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="regiaoId"    type="xs:long"/>
                <xs:element name="temperatura" type="xs:double"/>
                <xs:element name="umidade"     type="xs:double"/>
                <xs:element name="iuv"         type="xs:double"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:element name="processarLeituraResponse">
        <xs:complexType>
            <xs:sequence>
                <xs:element name="hri"         type="xs:double"/>
                <xs:element name="nivelAlerta" type="xs:string"/>
                <xs:element name="mensagem"    type="xs:string"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <xs:complexType name="AlertaDto">
        <xs:sequence>
            <xs:element name="id"       type="xs:long"/>
            <xs:element name="nivel"    type="xs:string"/>
            <xs:element name="mensagem" type="xs:string"/>
            <xs:element name="ativo"    type="xs:boolean"/>
            <xs:element name="nomeRegiao" type="xs:string"/>
        </xs:sequence>
    </xs:complexType>

</xs:schema>
```

---

### SentinelaSoapApplication.java

```java
package br.com.sentinela.soap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SentinelaSoapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SentinelaSoapApplication.class, args);
    }
}
```

---

### domain/model/Regiao.java (no soap)

```java
package br.com.sentinela.soap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "regiao")
@Data
@NoArgsConstructor
public class Regiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private Double latitude;
    private Double longitude;
    private String descricao;
}
```

---

### domain/model/LeituraClimatica.java (no soap)

```java
package br.com.sentinela.soap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leitura_climatica")
@Data
@NoArgsConstructor
public class LeituraClimatica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regiao_id")
    private Regiao regiao;

    private Double temperatura;
    private Double umidade;
    private Double iuv;
    private Double hri;
    private String fonte = "SOAP";
    private LocalDateTime timestamp = LocalDateTime.now();
}
```

---

### domain/model/Alerta.java (no soap — abstract)

```java
package br.com.sentinela.soap.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alerta")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "nivel")
@Data
@NoArgsConstructor
public abstract class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regiao_id")
    private Regiao regiao;

    @ManyToOne
    @JoinColumn(name = "leitura_id")
    private LeituraClimatica leitura;

    private String mensagem;
    private Boolean ativo = true;
    private LocalDateTime createdAt = LocalDateTime.now();

    public abstract String getNivel();
}
```

---

### domain/model/AlertaAtencao.java (no soap)

```java
package br.com.sentinela.soap.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ATENCAO")
public class AlertaAtencao extends Alerta {

    @Override
    public String getNivel() { return "ATENCAO"; }
}
```

---

### domain/model/AlertaCritico.java (no soap)

```java
package br.com.sentinela.soap.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CRITICO")
public class AlertaCritico extends Alerta {

    @Override
    public String getNivel() { return "CRITICO"; }
}
```

---

### repository/RegiaoRepository.java (no soap)

```java
package br.com.sentinela.soap.repository;

import br.com.sentinela.soap.domain.model.Regiao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegiaoRepository extends JpaRepository<Regiao, Long> {}
```

---

### repository/LeituraClimaticaRepository.java (no soap)

```java
package br.com.sentinela.soap.repository;

import br.com.sentinela.soap.domain.model.LeituraClimatica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeituraClimaticaRepository extends JpaRepository<LeituraClimatica, Long> {}
```

---

### repository/AlertaRepository.java (no soap)

```java
package br.com.sentinela.soap.repository;

import br.com.sentinela.soap.domain.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByRegiaoIdAndAtivoTrue(Long regiaoId);
}
```

---

### config/WebServiceConfig.java (no soap)

```java
package br.com.sentinela.soap.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "alerta")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema alertaSchema) {
        DefaultWsdl11Definition def = new DefaultWsdl11Definition();
        def.setPortTypeName("AlertaPort");
        def.setLocationUri("/ws");
        def.setTargetNamespace("http://sentinela.com.br/soap");
        def.setSchema(alertaSchema);
        return def;
    }

    @Bean
    public XsdSchema alertaSchema() {
        return new SimpleXsdSchema(new ClassPathResource("alerta.xsd"));
    }
}
```

---

### ws/AlertaWebService.java (no soap) — completo

```java
package br.com.sentinela.soap.ws;

import br.com.sentinela.soap.domain.model.*;
import br.com.sentinela.soap.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.bind.annotation.*;
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
            Alerta alerta = nivel.equals("CRITICO")
                    ? new AlertaCritico() : new AlertaAtencao();
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
        private Long regiaoId;
        private Double temperatura;
        private Double umidade;
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
        private Double hri;
        private String nivelAlerta;
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
        private Long id;
        private String nivel;
        private String mensagem;
        private Boolean ativo;
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
```

---

## Resumo dos endpoints após implementação completa

### sentinela-api (porta 8080)

| Método | Endpoint | Descrição | Auth |
|---|---|---|---|
| POST | /api/auth/register | Cadastro de usuário | ❌ |
| POST | /api/auth/login | Login, retorna JWT | ❌ |
| GET | /api/regioes | Lista regiões do usuário | ✅ |
| GET | /api/regioes/{id} | Busca região por ID | ✅ |
| POST | /api/regioes | Cria região | ✅ |
| PUT | /api/regioes/{id} | Atualiza região | ✅ |
| DELETE | /api/regioes/{id} | Remove região | ✅ |
| POST | /api/leituras | Processa leitura (chama SOAP) | ✅ |
| GET | /api/leituras/regiao/{id} | Lista leituras da região | ✅ |
| GET | /api/alertas | Lista todos os alertas ativos | ✅ |
| GET | /api/alertas/regiao/{id} | Lista alertas da região | ✅ |
| GET | /api/previsao/regiao/{id} | Previsão CPTEC da região | ✅ |

### sentinela-soap (porta 8081)

| Operação | Descrição |
|---|---|
| consultarAlertas | Retorna alertas ativos por regiaoId |
| processarLeitura | Calcula HRI, persiste leitura e alerta |

WSDL: `http://localhost:8081/ws/alerta?wsdl`

---

## Ordem de inicialização

```
1. docker compose up -d          (PostgreSQL)
2. ./mvnw spring-boot:run        (sentinela-soap — porta 8081)
3. ./mvnw spring-boot:run        (sentinela-api  — porta 8080)
4. npx expo start                (sentinela-mobile)
```

O SOAP deve subir antes da API REST porque o `SoapClientService` tenta conectar no startup.
