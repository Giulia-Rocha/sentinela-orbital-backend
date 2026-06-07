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
- **Containerização:** Docker & Docker Compose (Orquestração completa)

---

## 🚀 Como Executar

A solução está totalmente containerizada, o que facilita a execução e garante que todas as dependências (Banco e Serviços) subam corretamente.

### 1. Pré-requisitos
- **Docker** e **Docker Compose** instalados.
- **Java 21 JDK** (apenas para o build inicial dos arquivos .jar).

### 2. Build das Aplicações (Geração dos JARs)
Antes de subir os containers, é necessário gerar os pacotes das aplicações Java:
```bash
# Na raiz da pasta backend
./sentinela-soap/mvnw clean package -DskipTests
./sentinela-orbital/mvnw clean package -DskipTests
```

### 3. Subir o Ambiente Completo
Com os JARs gerados, execute o comando abaixo na raiz da pasta `backend`:
```bash
docker-compose up --build -d
```
O Docker Compose irá orquestrar a inicialização na ordem correta:
1.  **PostgreSQL:** Sobe o banco de dados.
2.  **Sentinela SOAP:** Aguarda o banco estar pronto e inicia o serviço.
3.  **Sentinela Orbital:** Aguarda o serviço SOAP estar saudável (healthcheck via WSDL) para então iniciar a API REST.

---

## 📖 Documentação e Acesso

### Portas e Endpoints
- **API REST (Orbital):** `http://localhost:8080`
  - **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Web Service SOAP:** `http://localhost:8081/ws`
  - **WSDL:** `http://localhost:8081/ws/alerta.wsdl`

### Operações SOAP
- Detalhes técnicos e exemplos de XML podem ser encontrados em: `DOCUMENTACAO_SOAP.md`

---

## 🌍 ODS Relacionados
- **ODS 11 – Cidades e Comunidades Sustentáveis:** Monitoramento de riscos climáticos urbanos.
- **ODS 13 – Ação Contra a Mudança Global do Clima:** Ferramenta de adaptação a eventos climáticos extremos.

---

## 👥 Equipe
- Gabriel Danius - RM 555747
- Caio Rossini - RM 555084
- Giulia Rocha- RM 558084
- Carlos Eduardo - RM 556785

---

## 📝 Conclusão
O projeto demonstra a interoperabilidade entre diferentes padrões de serviço (REST e SOAP), garantindo uma arquitetura robusta, segura e escalável para desafios climáticos reais utilizando tecnologia espacial.
