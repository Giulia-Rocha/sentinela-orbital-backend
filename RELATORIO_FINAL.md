# Relatório de Entrega: Global Solution 2026 - Space Connect (SOA)

**Projeto:** Sentinela Orbital & SOAP  
**Equipe:** [Inserir Nomes e RMs Aqui]

---

## 1. Descrição da Solução Proposta
O sistema **Sentinela** é uma solução de monitoramento climático avançado que integra dados de sensores terrestres com serviços espaciais (CPTEC/INPE). O foco principal é a detecção precoce de ondas de calor e radiação UV extrema, utilizando o índice HRI (Heat Risk Index) para disparar alertas automáticos para populações vulneráveis.

## 2. Problema Resolvido
A crise climática global tem aumentado a frequência de eventos de calor extremo, que causam milhares de mortes anualmente. Muitos municípios não possuem sistemas integrados que transformem dados brutos de satélite em alertas acionáveis. O projeto resolve isso através de uma arquitetura interoperável que processa dados complexos e disponibiliza informações simplificadas via Web e Mobile.

## 3. Objetivos da Aplicação
- Monitorar múltiplas regiões geográficas em tempo real.
- Integrar dados externos do CPTEC/INPE para previsões de 7 dias.
- Disponibilizar um serviço robusto de cálculo de risco (HRI) via SOAP para integração com outros sistemas de defesa civil.
- Prover uma API REST segura (JWT) para consumo por aplicações front-end e mobile.

## 4. Arquitetura da Solução e Diagrama SOA
A solução utiliza os princípios SOA:
- **Baixo Acoplamento:** O serviço de cálculo (SOAP) é independente da API de gerenciamento (REST).
- **Interoperabilidade:** Uso de JSON (REST) e XML (SOAP).
- **Contratos de Serviço:** Definidos via WSDL (SOAP) e OpenAPI/Swagger (REST).

*(Inserir aqui o Diagrama de Arquitetura SOA)*

## 5. Explicação da API REST
A API `sentinela-orbital` é o núcleo de gerenciamento:
- **Endpoints:** `/api/auth` (Autenticação), `/api/regioes` (CRUD de Regiões), `/api/leituras` (Processamento), `/api/alertas` (Consulta de Alertas ativos).
- **Tecnologias:** Java 21, Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, Hibernate, PostgreSQL.
- **Documentação:** Swagger UI interativo disponível em `http://localhost:8080/swagger-ui.html`.

## 6. Explicação do Web Service SOAP
O serviço `sentinela-soap` foca no processamento e lógica de negócio de alto nível:
- **WSDL:** Contrato formal para integração de sistemas legados de meteorologia.
- **Operações:** `processarLeitura` (Cálculo matemático de HRI) e `consultarAlertas`.
- **Tecnologias:** Spring Web Services, JAXB.

## 7. Integração entre Serviços
A aplicação `sentinela-orbital` atua como consumidora do serviço `sentinela-soap`. Ao receber uma nova leitura climática, a API REST chama o Web Service SOAP para validar os dados e calcular o risco. Além disso, a API REST consome o serviço externo do **CPTEC/INPE** via XML para buscar a previsão climática das cidades cadastradas.

## 8. Tecnologias Utilizadas
- **Backend:** Java 21, Spring Boot.
- **Banco de Dados:** PostgreSQL (Persistência) e Flyway (Migrações).
- **Segurança:** JSON Web Token (JWT).
- **Documentação:** Swagger/OpenAPI.
- **Interoperabilidade:** SOAP/XML e REST/JSON.

## 9. ODS Relacionados
- **ODS 11 – Cidades e Comunidades Sustentáveis:** Monitoramento de riscos urbanos.
- **ODS 13 – Ação Contra a Mudança Global do Clima:** Adaptação a eventos climáticos extremos.

## 10. Evidências de Funcionamento
*(Inserir aqui os prints dos testes: Swagger UI, Postman/SoapUI com requisições SOAP, e logs da integração)*

---

## 11. Conclusão
O projeto demonstra com sucesso a aplicação de conceitos de SOA em um cenário real e urgente. A separação de responsabilidades entre serviços REST e SOAP permitiu criar uma arquitetura escalável, segura e pronta para integração com ecossistemas tecnológicos diversos no setor espacial e meteorológico.
