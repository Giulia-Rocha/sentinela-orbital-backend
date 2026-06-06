# Documentação do Web Service SOAP - Sentinela SOAP

Este documento descreve as operações disponíveis no serviço SOAP do projeto Space Connect, utilizado para processamento pesado de dados climáticos e consulta de alertas.

## Endpoint e WSDL
- **URL do Serviço:** `http://localhost:8081/ws`
- **WSDL:** `http://localhost:8081/ws/alerta.wsdl`

## Operações Disponíveis

### 1. Processar Leitura (`processarLeituraRequest`)
Calcula o índice HRI (Heat Risk Index) com base em dados de sensores ou satélites e gera um alerta se necessário.

**Exemplo de Requisição (XML):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sen="http://sentinela.com.br/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <sen:processarLeituraRequest>
         <sen:regiaoId>1</sen:regiaoId>
         <sen:temperatura>38.5</sen:temperatura>
         <sen:umidade>20.0</sen:umidade>
         <sen:iuv>9.0</sen:iuv>
      </sen:processarLeituraRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Exemplo de Resposta (XML):**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:processarLeituraResponse xmlns:ns2="http://sentinela.com.br/soap">
         <ns2:hri>7.8</ns2:hri>
         <ns2:nivelAlerta>ALERTA</ns2:nivelAlerta>
         <ns2:mensagem>ALERTA: Temperatura 38.5°C, HRI 7.8. Condições de calor intenso.</ns2:mensagem>
      </ns2:processarLeituraResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

### 2. Consultar Alertas (`consultarAlertasRequest`)
Retorna a lista de alertas ativos para uma determinada região.

**Exemplo de Requisição (XML):**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:sen="http://sentinela.com.br/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <sen:consultarAlertasRequest>
         <sen:regiaoId>1</sen:regiaoId>
      </sen:consultarAlertasRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Exemplo de Resposta (XML):**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:consultarAlertasResponse xmlns:ns2="http://sentinela.com.br/soap">
         <ns2:alertas>
            <ns2:id>1</ns2:id>
            <ns2:nivel>CRITICO</ns2:nivel>
            <ns2:mensagem>ALERTA CRÍTICO: Risco extremo detectado.</ns2:mensagem>
            <ns2:ativo>true</ns2:ativo>
            <ns2:nomeRegiao>Centro de São Paulo</ns2:nomeRegiao>
         </ns2:alertas>
      </ns2:consultarAlertasResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

## Instruções de Teste
1. Inicie o serviço `sentinela-soap` (Porta 8081).
2. Utilize o **SoapUI** ou o **Postman** (Importando o WSDL).
3. Certifique-se de que o banco de dados PostgreSQL esteja rodando e com os dados iniciais do script `V2__seed.sql`.
