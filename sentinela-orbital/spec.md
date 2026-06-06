#####
this file shows how create the folder arch on the project


sentinela-api/
└── src/main/java/br/com/sentinela/
├── SentinelaApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── WebServiceConfig.java
├── controller/
│   ├── AuthController.java
│   ├── RegiaoController.java
│   └── AlertaController.java
├── ws/
│   ├── AlertaWebService.java
│   └── dto/
│       ├── ConsultaAlertaRequest.java
│       ├── ConsultaAlertaResponse.java
│       ├── ProcessaLeituraRequest.java
│       └── ProcessaLeituraResponse.java
├── service/
│   ├── AuthService.java
│   ├── RegiaoService.java
│   ├── AlertaService.java
│   └── CptecService.java
├── repository/
│   ├── UsuarioRepository.java
│   ├── RegiaoRepository.java
│   ├── LeituraClimaticaRepository.java
│   └── AlertaRepository.java
├── domain/
│   ├── model/
│   │   ├── Usuario.java
│   │   ├── Regiao.java
│   │   ├── LeituraClimatica.java
│   │   ├── Alerta.java
│   │   ├── AlertaAtencao.java
│   │   └── AlertaCritico.java
│   └── dto/
│       ├── request/
│       │   ├── LoginRequest.java
│       │   ├── RegisterRequest.java
│       │   └── RegiaoRequest.java
│       └── response/
│           ├── TokenResponse.java
│           ├── RegiaoResponse.java
│           └── AlertaResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── RegiaoNotFoundException.java
└── security/
├── JwtTokenProvider.java
├── JwtAuthFilter.java
└── UserDetailsServiceImpl.java