# 🛰️ Sentinela Orbital & SOAP - Space Connect

Este repositório contém a solução back-end para a **Global Solution 2026 - Space Connect (SOA)**. O sistema monitora condições climáticas terrestres integrando dados espaciais para detecção precoce de ondas de calor.

---

## 🏗️ Arquitetura do Projeto

A solução segue os princípios de **Arquitetura Orientada a Serviços (SOA)**, dividida em dois serviços principais que se comunicam de forma interoperável:

1.  **Sentinela Orbital (REST API):** Núcleo de gerenciamento, autenticação e consumo de dados externos.
2.  **Sentinela SOAP (Web Service):** Serviço especializado em processamento pesado e cálculo do Heat Risk Index (HRI).

### Tecnologias Utilizadas
- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.x
- **Banco de Dados:** PostgreSQL 16
- **Segurança:** Spring Security + JWT (JSON Web Token)
- **Documentação:** Swagger/OpenAPI (REST) e WSDL (SOAP)
- **Integração:** REST, SOAP/XML e consumo de API externa (CPTEC/INPE)
- **Containerização:** Docker & Docker Compose

---

## 🚀 Como Executar

### 1. Pré-requisitos
- Docker e Docker Compose instalados.
- Java 21 JDK.
- Maven (ou use o `./mvnw` incluso).

### 2. Subir o Banco de Dados
Na raiz da pasta `backend`, execute:
```bash
docker-compose up -d
```

### 3. Iniciar o Serviço SOAP (Porta 8081)
O serviço SOAP deve ser iniciado primeiro, pois a API REST valida a conexão no startup.
```bash
cd sentinela-soap
./mvnw spring-boot:run
```
- **WSDL disponível em:** `http://localhost:8081/ws/alerta.wsdl`

### 4. Iniciar a API REST (Porta 8080)
```bash
cd sentinela-orbital
./mvnw spring-boot:run
```
- **Documentação Interativa (Swagger):** `http://localhost:8080/swagger-ui.html`

---

## 📖 Documentação das APIs

### API REST (Sentinela Orbital)
- **Autenticação:** `/api/auth/**` (Permitido sem token)
- **Regiões:** `/api/regioes` (CRUD completo de regiões monitoradas)
- **Leituras:** `/api/leituras` (Recebe dados climáticos e integra com SOAP)
- **Previsão:** `/api/previsao` (Consome dados XML do CPTEC/INPE)
- **Alertas:** `/api/alertas` (Lista alertas ativos gerados pelo sistema)

### Web Service SOAP (Sentinela SOAP)
- **Operação `processarLeitura`:** Calcula o risco HRI e gera alertas automáticos.
- **Operação `consultarAlertas`:** Consulta técnica de alertas para integração sistêmica.
- *Detalhes técnicos em:* `DOCUMENTACAO_SOAP.md`

---

## 🌍 ODS Relacionados
- **ODS 11 – Cidades e Comunidades Sustentáveis:** Monitoramento de riscos climáticos urbanos.
- **ODS 13 – Ação Contra a Mudança Global do Clima:** Ferramenta de adaptação a eventos climáticos extremos.

---

## 👥 Equipe
- **Nome do Integrante 1** - RMXXXXX
- **Nome do Integrante 2** - RMXXXXX

---

## 📝 Conclusão
O projeto demonstra a interoperabilidade entre diferentes padrões de serviço (REST e SOAP), garantindo uma arquitetura robusta, segura e escalável para desafios climáticos reais utilizando tecnologia espacial.
