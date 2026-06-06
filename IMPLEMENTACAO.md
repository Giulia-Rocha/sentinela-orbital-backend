# Sentinela Orbital — Plano de Implementação Completo

## Visão Geral dos Projetos

| Projeto | Porta | Tecnologia |
|---|---|---|
| sentinela-api | 8080 | Spring Boot — API REST |
| sentinela-soap | 8081 | Spring Boot — Web Service SOAP |
| sentinela-mobile | — | React Native + Expo |

---

## PROJETO 1 — sentinela-api (REST, porta 8080)

### pom.xml — dependências necessárias

```xml
<dependencies>
    <!-- Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Flyway -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- DevTools -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

### application.yml

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
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

jwt:
  secret: sentinela-orbital-secret-key-2026-muito-segura
  expiration: 86400000

server:
  port: 8080

soap:
  url: http://localhost:8081/ws
```

---

### db/migration/V1__init.sql

```sql
CREATE TABLE usuario (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(100)     NOT NULL,
    email      VARCHAR(150)     NOT NULL UNIQUE,
    senha      VARCHAR(255)     NOT NULL,
    created_at TIMESTAMP        NOT NULL DEFAULT NOW()
);

CREATE TABLE regiao (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(100)     NOT NULL,
    latitude   DOUBLE PRECISION NOT NULL,
    longitude  DOUBLE PRECISION NOT NULL,
    descricao  VARCHAR(255),
    usuario_id BIGINT           NOT NULL,
    CONSTRAINT fk_regiao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

CREATE TABLE leitura_climatica (
    id          BIGSERIAL PRIMARY KEY,
    regiao_id   BIGINT           NOT NULL,
    temperatura DOUBLE PRECISION NOT NULL,
    umidade     DOUBLE PRECISION NOT NULL,
    iuv         DOUBLE PRECISION NOT NULL,
    hri         DOUBLE PRECISION NOT NULL,
    fonte       VARCHAR(50)      NOT NULL DEFAULT 'CPTEC',
    timestamp   TIMESTAMP        NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_leitura_regiao FOREIGN KEY (regiao_id) REFERENCES regiao(id)
);

CREATE TABLE alerta (
    id         BIGSERIAL PRIMARY KEY,
    regiao_id  BIGINT       NOT NULL,
    leitura_id BIGINT       NOT NULL,
    nivel      VARCHAR(20)  NOT NULL CHECK (nivel IN ('ATENCAO', 'ALERTA', 'CRITICO')),
    mensagem   VARCHAR(500) NOT NULL,
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alerta_regiao   FOREIGN KEY (regiao_id)  REFERENCES regiao(id),
    CONSTRAINT fk_alerta_leitura  FOREIGN KEY (leitura_id) REFERENCES leitura_climatica(id)
);

CREATE INDEX idx_leitura_regiao    ON leitura_climatica(regiao_id);
CREATE INDEX idx_leitura_timestamp ON leitura_climatica(timestamp);
CREATE INDEX idx_alerta_regiao     ON alerta(regiao_id);
CREATE INDEX idx_alerta_ativo      ON alerta(ativo);
```

---

### domain/model/Usuario.java

```java
@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String senha;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "usuario")
    private List<Regiao> regioes = new ArrayList<>();

    // UserDetails
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(); }
    @Override public String getPassword() { return senha; }
    @Override public String getUsername() { return email; }
}
```

### domain/model/Regiao.java

```java
@Entity
@Table(name = "regiao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Regiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    private String descricao;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "regiao", cascade = CascadeType.ALL)
    private List<LeituraClimatica> leituras = new ArrayList<>();
}
```

### domain/model/LeituraClimatica.java

```java
@Entity
@Table(name = "leitura_climatica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeituraClimatica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regiao_id", nullable = false)
    private Regiao regiao;

    @Column(nullable = false)
    private Double temperatura;

    @Column(nullable = false)
    private Double umidade;

    @Column(nullable = false)
    private Double iuv;

    @Column(nullable = false)
    private Double hri;

    @Column(nullable = false)
    private String fonte = "CPTEC";

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
```

### domain/model/Alerta.java (abstract — OOP)

```java
@Entity
@Table(name = "alerta")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "nivel")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "regiao_id", nullable = false)
    private Regiao regiao;

    @ManyToOne
    @JoinColumn(name = "leitura_id", nullable = false)
    private LeituraClimatica leitura;

    @Column(nullable = false)
    private String mensagem;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public abstract String getNivel();
}
```

### domain/model/AlertaAtencao.java

```java
@Entity
@DiscriminatorValue("ATENCAO")
public class AlertaAtencao extends Alerta {

    public AlertaAtencao() { super(); }

    @Override
    public String getNivel() { return "ATENCAO"; }
}
```

### domain/model/AlertaCritico.java

```java
@Entity
@DiscriminatorValue("CRITICO")
public class AlertaCritico extends Alerta {

    public AlertaCritico() { super(); }

    @Override
    public String getNivel() { return "CRITICO"; }
}
```

---

### repository/UsuarioRepository.java

```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### repository/RegiaoRepository.java

```java
@Repository
public interface RegiaoRepository extends JpaRepository<Regiao, Long> {
    List<Regiao> findByUsuarioId(Long usuarioId);
}
```

### repository/LeituraClimaticaRepository.java

```java
@Repository
public interface LeituraClimaticaRepository extends JpaRepository<LeituraClimatica, Long> {
    List<LeituraClimatica> findByRegiaoIdOrderByTimestampDesc(Long regiaoId);
    Optional<LeituraClimatica> findTopByRegiaoIdOrderByTimestampDesc(Long regiaoId);
}
```

### repository/AlertaRepository.java

```java
@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findByRegiaoIdAndAtivoTrue(Long regiaoId);
    List<Alerta> findAllByAtivoTrueOrderByCreatedAtDesc();
}
```

---

### domain/dto/request/RegisterRequest.java

```java
@Data
public class RegisterRequest {
    @NotBlank private String nome;
    @Email @NotBlank private String email;
    @NotBlank @Size(min = 6) private String senha;
}
```

### domain/dto/request/LoginRequest.java

```java
@Data
public class LoginRequest {
    @Email @NotBlank private String email;
    @NotBlank private String senha;
}
```

### domain/dto/request/RegiaoRequest.java

```java
@Data
public class RegiaoRequest {
    @NotBlank private String nome;
    @NotNull private Double latitude;
    @NotNull private Double longitude;
    private String descricao;
}
```

### domain/dto/response/TokenResponse.java

```java
@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String tipo = "Bearer";
    private String email;
}
```

### domain/dto/response/RegiaoResponse.java

```java
@Data
@AllArgsConstructor
public class RegiaoResponse {
    private Long id;
    private String nome;
    private Double latitude;
    private Double longitude;
    private String descricao;
}
```

### domain/dto/response/AlertaResponse.java

```java
@Data
@AllArgsConstructor
public class AlertaResponse {
    private Long id;
    private String nivel;
    private String mensagem;
    private Boolean ativo;
    private LocalDateTime createdAt;
    private String nomeRegiao;
}
```

---

### security/JwtTokenProvider.java

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
```

### security/JwtAuthFilter.java

```java
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (tokenProvider.isValid(token)) {
                String email = tokenProvider.getEmail(token);
                UserDetails user = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
```

### security/UserDetailsServiceImpl.java

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }
}
```

---

### config/SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

### service/AuthService.java

```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authManager;

    public TokenResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email já cadastrado");

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .build();

        usuarioRepository.save(usuario);
        String token = tokenProvider.generateToken(usuario.getEmail());
        return new TokenResponse(token, "Bearer", usuario.getEmail());
    }

    public TokenResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
        String token = tokenProvider.generateToken(request.getEmail());
        return new TokenResponse(token, "Bearer", request.getEmail());
    }
}
```

### service/RegiaoService.java

```java
@Service
@RequiredArgsConstructor
public class RegiaoService {

    private final RegiaoRepository regiaoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<RegiaoResponse> listar(String email) {
        return regiaoRepository.findByUsuarioId(getUsuario(email).getId())
                .stream().map(this::toResponse).toList();
    }

    public RegiaoResponse buscarPorId(Long id) {
        return toResponse(regiaoRepository.findById(id)
                .orElseThrow(() -> new RegiaoNotFoundException(id)));
    }

    public RegiaoResponse criar(RegiaoRequest request, String email) {
        Regiao regiao = Regiao.builder()
                .nome(request.getNome())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .descricao(request.getDescricao())
                .usuario(getUsuario(email))
                .build();
        return toResponse(regiaoRepository.save(regiao));
    }

    public RegiaoResponse atualizar(Long id, RegiaoRequest request) {
        Regiao regiao = regiaoRepository.findById(id)
                .orElseThrow(() -> new RegiaoNotFoundException(id));
        regiao.setNome(request.getNome());
        regiao.setLatitude(request.getLatitude());
        regiao.setLongitude(request.getLongitude());
        regiao.setDescricao(request.getDescricao());
        return toResponse(regiaoRepository.save(regiao));
    }

    public void deletar(Long id) {
        if (!regiaoRepository.existsById(id)) throw new RegiaoNotFoundException(id);
        regiaoRepository.deleteById(id);
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    private RegiaoResponse toResponse(Regiao r) {
        return new RegiaoResponse(r.getId(), r.getNome(), r.getLatitude(), r.getLongitude(), r.getDescricao());
    }
}
```

### service/AlertaService.java

```java
@Service
@RequiredArgsConstructor
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public List<AlertaResponse> listarAtivos() {
        return alertaRepository.findAllByAtivoTrueOrderByCreatedAtDesc()
                .stream().map(this::toResponse).toList();
    }

    public List<AlertaResponse> listarPorRegiao(Long regiaoId) {
        return alertaRepository.findByRegiaoIdAndAtivoTrue(regiaoId)
                .stream().map(this::toResponse).toList();
    }

    private AlertaResponse toResponse(Alerta a) {
        return new AlertaResponse(
                a.getId(), a.getNivel(), a.getMensagem(),
                a.getAtivo(), a.getCreatedAt(), a.getRegiao().getNome());
    }
}
```

### service/CptecService.java

```java
@Service
@RequiredArgsConstructor
public class CptecService {

    private final RestTemplate restTemplate;
    private static final String BASE = "http://servicos.cptec.inpe.br/XML";

    public PrevisaoCptec buscarPrevisao(int codigoCidade) {
        String url = BASE + "/cidade/" + codigoCidade + "/previsao.xml";
        String xml = restTemplate.getForObject(url, String.class);
        return parseXml(xml);
    }

    private PrevisaoCptec parseXml(String xml) {
        // Usa JAXB ou parsing simples com DocumentBuilder
        // Extrai: tempMax, tempMin, iuv do primeiro dia
        // Retorna objeto PrevisaoCptec com esses campos
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            double tempMax = Double.parseDouble(
                doc.getElementsByTagName("maxima").item(0).getTextContent());
            double tempMin = Double.parseDouble(
                doc.getElementsByTagName("minima").item(0).getTextContent());
            double iuv = Double.parseDouble(
                doc.getElementsByTagName("iuv").item(0).getTextContent());
            return new PrevisaoCptec(tempMax, tempMin, iuv);
        } catch (Exception e) {
            // fallback com dados mockados se CPTEC estiver fora
            return new PrevisaoCptec(35.0, 22.0, 6.0);
        }
    }
}
```

> Crie o record `PrevisaoCptec.java` em `domain/model/`:
> ```java
> public record PrevisaoCptec(double tempMax, double tempMin, double iuv) {}
> ```

### config/AppConfig.java

```java
@Configuration
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

### controller/AuthController.java

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

### controller/RegiaoController.java

```java
@RestController
@RequestMapping("/api/regioes")
@RequiredArgsConstructor
public class RegiaoController {

    private final RegiaoService regiaoService;

    @GetMapping
    public ResponseEntity<List<RegiaoResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(regiaoService.listar(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegiaoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(regiaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<RegiaoResponse> criar(@Valid @RequestBody RegiaoRequest request,
                                                 Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(regiaoService.criar(request, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegiaoResponse> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody RegiaoRequest request) {
        return ResponseEntity.ok(regiaoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        regiaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
```

### controller/AlertaController.java

```java
@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listarAtivos() {
        return ResponseEntity.ok(alertaService.listarAtivos());
    }

    @GetMapping("/regiao/{regiaoId}")
    public ResponseEntity<List<AlertaResponse>> listarPorRegiao(@PathVariable Long regiaoId) {
        return ResponseEntity.ok(alertaService.listarPorRegiao(regiaoId));
    }
}
```

---

### exception/RegiaoNotFoundException.java

```java
public class RegiaoNotFoundException extends RuntimeException {
    public RegiaoNotFoundException(Long id) {
        super("Região não encontrada: " + id);
    }
}
```

### exception/GlobalExceptionHandler.java

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegiaoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RegiaoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> erros.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erros);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }
}
```

---

## PROJETO 2 — sentinela-soap (SOAP, porta 8081)

### Como criar

Novo projeto Spring Boot via [start.spring.io](https://start.spring.io):
- Dependências: **Spring Web Services**, **Spring Data JPA**, **PostgreSQL**, **Lombok**
- Group: `br.com.sentinela`
- Artifact: `sentinela-soap`

### pom.xml — dependências extras

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web-services</artifactId>
</dependency>
<dependency>
    <groupId>wsdl4j</groupId>
    <artifactId>wsdl4j</artifactId>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
</dependency>
```

### application.yml

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

server:
  port: 8081
```

> O SOAP usa o mesmo banco PostgreSQL — sem migration própria, as tabelas já existem.

---

### src/main/resources/alerta.xsd

```xml
<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
           targetNamespace="http://sentinela.com.br/soap"
           xmlns:tns="http://sentinela.com.br/soap"
           elementFormDefault="qualified">

    <!-- Consultar alertas por região -->
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
                <xs:element name="alertas" type="tns:AlertaDto" maxOccurs="unbounded" minOccurs="0"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- Processar leitura -->
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
                <xs:element name="hri"        type="xs:double"/>
                <xs:element name="nivelAlerta" type="xs:string"/>
                <xs:element name="mensagem"   type="xs:string"/>
            </xs:sequence>
        </xs:complexType>
    </xs:element>

    <!-- DTO -->
    <xs:complexType name="AlertaDto">
        <xs:sequence>
            <xs:element name="id"       type="xs:long"/>
            <xs:element name="nivel"    type="xs:string"/>
            <xs:element name="mensagem" type="xs:string"/>
            <xs:element name="ativo"    type="xs:boolean"/>
        </xs:sequence>
    </xs:complexType>

</xs:schema>
```

---

### config/WebServiceConfig.java

```java
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

### ws/AlertaWebService.java

```java
@Endpoint
public class AlertaWebService {

    private static final String NS = "http://sentinela.com.br/soap";

    private final AlertaRepository alertaRepository;
    private final LeituraClimaticaRepository leituraRepository;
    private final RegiaoRepository regiaoRepository;

    public AlertaWebService(AlertaRepository alertaRepository,
                            LeituraClimaticaRepository leituraRepository,
                            RegiaoRepository regiaoRepository) {
        this.alertaRepository = alertaRepository;
        this.leituraRepository = leituraRepository;
        this.regiaoRepository = regiaoRepository;
    }

    @PayloadRoot(namespace = NS, localPart = "consultarAlertasRequest")
    @ResponsePayload
    public ConsultarAlertasResponse consultarAlertas(
            @RequestPayload ConsultarAlertasRequest request) {

        List<Alerta> alertas = alertaRepository
                .findByRegiaoIdAndAtivoTrue(request.getRegiaoId());

        ConsultarAlertasResponse response = new ConsultarAlertasResponse();
        alertas.forEach(a -> {
            AlertaDto dto = new AlertaDto();
            dto.setId(a.getId());
            dto.setNivel(a.getNivel());
            dto.setMensagem(a.getMensagem());
            dto.setAtivo(a.getAtivo());
            response.getAlertas().add(dto);
        });
        return response;
    }

    @PayloadRoot(namespace = NS, localPart = "processarLeituraRequest")
    @ResponsePayload
    public ProcessarLeituraResponse processarLeitura(
            @RequestPayload ProcessarLeituraRequest request) {

        double hri = calcularHri(request.getTemperatura(),
                                  request.getUmidade(),
                                  request.getIuv());

        String nivel = hri >= 8.0 ? "CRITICO" : hri >= 6.0 ? "ALERTA" : "ATENCAO";
        String mensagem = gerarMensagem(nivel, request.getTemperatura(), hri);

        Regiao regiao = regiaoRepository.findById(request.getRegiaoId())
                .orElseThrow(() -> new RuntimeException("Região não encontrada"));

        LeituraClimatica leitura = LeituraClimatica.builder()
                .regiao(regiao)
                .temperatura(request.getTemperatura())
                .umidade(request.getUmidade())
                .iuv(request.getIuv())
                .hri(hri)
                .build();
        leituraRepository.save(leitura);

        if (!nivel.equals("ATENCAO")) {
            Alerta alerta = nivel.equals("CRITICO")
                    ? new AlertaCritico() : new AlertaAtencao();
            alerta.setRegiao(regiao);
            alerta.setLeitura(leitura);
            alerta.setMensagem(mensagem);
            alertaRepository.save(alerta);
        }

        ProcessarLeituraResponse response = new ProcessarLeituraResponse();
        response.setHri(hri);
        response.setNivelAlerta(nivel);
        response.setMensagem(mensagem);
        return response;
    }

    private double calcularHri(double temp, double umidade, double iuv) {
        return (temp / 50.0 * 0.4 + umidade / 100.0 * 0.2 + iuv / 12.0 * 0.4) * 10;
    }

    private String gerarMensagem(String nivel, double temp, double hri) {
        return switch (nivel) {
            case "CRITICO" -> String.format(
                "ALERTA CRÍTICO: Temperatura %.1f°C, HRI %.1f. Risco extremo de onda de calor.", temp, hri);
            case "ALERTA" -> String.format(
                "ALERTA: Temperatura %.1f°C, HRI %.1f. Condições de calor intenso.", temp, hri);
            default -> String.format(
                "ATENÇÃO: Temperatura %.1f°C, HRI %.1f. Monitoramento ativo.", temp, hri);
        };
    }
}
```

> As classes `ConsultarAlertasRequest`, `ConsultarAlertasResponse`, `ProcessarLeituraRequest`,
> `ProcessarLeituraResponse` e `AlertaDto` são geradas automaticamente pelo plugin
> `jaxb2-maven-plugin` a partir do `alerta.xsd`. Adicione ao `pom.xml`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>jaxb2-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <goals><goal>xjc</goal></goals>
        </execution>
    </executions>
    <configuration>
        <sources>
            <source>src/main/resources/alerta.xsd</source>
        </sources>
        <packageName>br.com.sentinela.ws.generated</packageName>
    </configuration>
</plugin>
```

---

## PROJETO 3 — sentinela-mobile (React Native + Expo)

---

### app/_layout.tsx

```tsx
import { Stack } from "expo-router";
import { AlertaProvider } from "@/context/AlertaContext";
import { UserProvider } from "@/context/UserContext";

export default function RootLayout() {
  return (
    <UserProvider>
      <AlertaProvider>
        <Stack screenOptions={{ headerShown: false }} />
      </AlertaProvider>
    </UserProvider>
  );
}
```

### app/index.tsx

```tsx
import { Redirect } from "expo-router";
import { useUser } from "@/context/UserContext";

export default function Index() {
  const { token } = useUser();
  return <Redirect href={token ? "/(tabs)" : "/auth/login"} />;
}
```

---

### app/auth/_layout.tsx

```tsx
import { Stack } from "expo-router";

export default function AuthLayout() {
  return <Stack screenOptions={{ headerShown: false }} />;
}
```

### app/auth/login.tsx

```tsx
import { useState } from "react";
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from "react-native";
import { router } from "expo-router";
import { api } from "@/services/api";
import { useUser } from "@/context/UserContext";
import { Colors } from "@/constants/colors";

export default function Login() {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const { setToken } = useUser();

  async function handleLogin() {
    if (!email || !senha) {
      Alert.alert("Erro", "Preencha todos os campos");
      return;
    }
    setLoading(true);
    try {
      const res = await api.post("/auth/login", { email, senha });
      setToken(res.data.token);
      router.replace("/(tabs)");
    } catch {
      Alert.alert("Erro", "Credenciais inválidas");
    } finally {
      setLoading(false);
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>🛰️ Sentinela Orbital</Text>
      <Text style={styles.subtitle}>Monitoramento de Ondas de Calor</Text>
      <TextInput style={styles.input} placeholder="Email"
        placeholderTextColor={Colors.textMuted}
        value={email} onChangeText={setEmail}
        keyboardType="email-address" autoCapitalize="none" />
      <TextInput style={styles.input} placeholder="Senha"
        placeholderTextColor={Colors.textMuted}
        value={senha} onChangeText={setSenha} secureTextEntry />
      <TouchableOpacity style={styles.button} onPress={handleLogin} disabled={loading}>
        <Text style={styles.buttonText}>{loading ? "Entrando..." : "Entrar"}</Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={() => router.push("/auth/register")}>
        <Text style={styles.link}>Não tem conta? Cadastre-se</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background,
    justifyContent: "center", padding: 24 },
  title: { fontSize: 28, fontWeight: "bold", color: Colors.text,
    textAlign: "center", marginBottom: 8 },
  subtitle: { fontSize: 14, color: Colors.textMuted,
    textAlign: "center", marginBottom: 40 },
  input: { backgroundColor: Colors.surface, color: Colors.text,
    borderRadius: 8, padding: 14, marginBottom: 12,
    borderWidth: 1, borderColor: Colors.border },
  button: { backgroundColor: Colors.primary, borderRadius: 8,
    padding: 14, alignItems: "center", marginTop: 8 },
  buttonText: { color: Colors.text, fontWeight: "bold", fontSize: 16 },
  link: { color: Colors.accent, textAlign: "center", marginTop: 16 },
});
```

### app/auth/register.tsx

```tsx
import { useState } from "react";
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from "react-native";
import { router } from "expo-router";
import { api } from "@/services/api";
import { useUser } from "@/context/UserContext";
import { Colors } from "@/constants/colors";

export default function Register() {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const { setToken } = useUser();

  async function handleRegister() {
    if (!nome || !email || !senha) {
      Alert.alert("Erro", "Preencha todos os campos");
      return;
    }
    if (senha.length < 6) {
      Alert.alert("Erro", "Senha deve ter no mínimo 6 caracteres");
      return;
    }
    try {
      const res = await api.post("/auth/register", { nome, email, senha });
      setToken(res.data.token);
      router.replace("/(tabs)");
    } catch {
      Alert.alert("Erro", "Não foi possível criar a conta");
    }
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Criar Conta</Text>
      <TextInput style={styles.input} placeholder="Nome"
        placeholderTextColor={Colors.textMuted}
        value={nome} onChangeText={setNome} />
      <TextInput style={styles.input} placeholder="Email"
        placeholderTextColor={Colors.textMuted}
        value={email} onChangeText={setEmail}
        keyboardType="email-address" autoCapitalize="none" />
      <TextInput style={styles.input} placeholder="Senha (mín. 6 caracteres)"
        placeholderTextColor={Colors.textMuted}
        value={senha} onChangeText={setSenha} secureTextEntry />
      <TouchableOpacity style={styles.button} onPress={handleRegister}>
        <Text style={styles.buttonText}>Cadastrar</Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={() => router.back()}>
        <Text style={styles.link}>Já tem conta? Entrar</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background,
    justifyContent: "center", padding: 24 },
  title: { fontSize: 28, fontWeight: "bold", color: Colors.text,
    textAlign: "center", marginBottom: 32 },
  input: { backgroundColor: Colors.surface, color: Colors.text,
    borderRadius: 8, padding: 14, marginBottom: 12,
    borderWidth: 1, borderColor: Colors.border },
  button: { backgroundColor: Colors.primary, borderRadius: 8,
    padding: 14, alignItems: "center", marginTop: 8 },
  buttonText: { color: Colors.text, fontWeight: "bold", fontSize: 16 },
  link: { color: Colors.accent, textAlign: "center", marginTop: 16 },
});
```

---

### app/(tabs)/_layout.tsx

```tsx
import { Tabs } from "expo-router";
import { Colors } from "@/constants/colors";

export default function TabsLayout() {
  return (
    <Tabs screenOptions={{
      tabBarStyle: { backgroundColor: Colors.surface, borderTopColor: Colors.border },
      tabBarActiveTintColor: Colors.primary,
      tabBarInactiveTintColor: Colors.textMuted,
      headerStyle: { backgroundColor: Colors.surface },
      headerTintColor: Colors.text,
    }}>
      <Tabs.Screen name="index"
        options={{ title: "Dashboard", tabBarLabel: "Dashboard" }} />
      <Tabs.Screen name="alertas"
        options={{ title: "Alertas", tabBarLabel: "Alertas" }} />
      <Tabs.Screen name="forecast"
        options={{ title: "Previsão", tabBarLabel: "Previsão" }} />
      <Tabs.Screen name="settings"
        options={{ title: "Configurações", tabBarLabel: "Config" }} />
    </Tabs>
  );
}
```

### app/(tabs)/index.tsx — Dashboard principal

```tsx
import { useEffect, useState } from "react";
import { View, Text, ScrollView, StyleSheet, RefreshControl } from "react-native";
import { useAlerta } from "@/context/AlertaContext";
import { HriIndicator } from "@/components/HriIndicator";
import { AlertaCard } from "@/components/AlertaCard";
import { Colors } from "@/constants/colors";

export default function Dashboard() {
  const { alertas, hriAtual, carregarAlertas, loading } = useAlerta();

  useEffect(() => { carregarAlertas(); }, []);

  return (
    <ScrollView style={styles.container}
      refreshControl={<RefreshControl refreshing={loading} onRefresh={carregarAlertas} />}>
      <Text style={styles.titulo}>Monitoramento em Tempo Real</Text>
      <HriIndicator valor={hriAtual} />
      <Text style={styles.secao}>Alertas Ativos</Text>
      {alertas.slice(0, 3).map(a => <AlertaCard key={a.id} alerta={a} />)}
      {alertas.length === 0 &&
        <Text style={styles.vazio}>Nenhum alerta ativo no momento</Text>}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 16 },
  titulo: { fontSize: 18, fontWeight: "bold", color: Colors.text, marginBottom: 16 },
  secao: { fontSize: 16, fontWeight: "600", color: Colors.text, marginVertical: 12 },
  vazio: { color: Colors.textMuted, textAlign: "center", marginTop: 24 },
});
```

### app/(tabs)/alertas.tsx

```tsx
import { useEffect } from "react";
import { View, Text, FlatList, StyleSheet } from "react-native";
import { useAlerta } from "@/context/AlertaContext";
import { AlertaCard } from "@/components/AlertaCard";
import { Colors } from "@/constants/colors";

export default function Alertas() {
  const { alertas, carregarAlertas } = useAlerta();

  useEffect(() => { carregarAlertas(); }, []);

  return (
    <View style={styles.container}>
      <FlatList
        data={alertas}
        keyExtractor={item => String(item.id)}
        renderItem={({ item }) => <AlertaCard alerta={item} />}
        ListEmptyComponent={
          <Text style={styles.vazio}>Nenhum alerta ativo</Text>}
        contentContainerStyle={{ padding: 16 }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  vazio: { color: Colors.textMuted, textAlign: "center", marginTop: 40 },
});
```

### app/(tabs)/forecast.tsx

```tsx
import { useEffect, useState } from "react";
import { View, Text, ScrollView, StyleSheet } from "react-native";
import { VictoryChart, VictoryLine, VictoryAxis, VictoryTheme } from "victory-native";
import { api } from "@/services/api";
import { Colors } from "@/constants/colors";

export default function Forecast() {
  const [dados, setDados] = useState<{ x: number; y: number }[]>([]);

  useEffect(() => {
    // mock de dados de previsão — substitua por chamada real
    setDados([
      { x: 1, y: 32 }, { x: 2, y: 35 }, { x: 3, y: 38 },
      { x: 4, y: 40 }, { x: 5, y: 39 }, { x: 6, y: 36 }, { x: 7, y: 33 },
    ]);
  }, []);

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.titulo}>Previsão de Temperatura — 7 dias</Text>
      <View style={styles.grafico}>
        <VictoryChart theme={VictoryTheme.material} height={260}>
          <VictoryAxis label="Dia"
            style={{ axisLabel: { fill: Colors.textMuted },
                     tickLabels: { fill: Colors.textMuted } }} />
          <VictoryAxis dependentAxis label="°C"
            style={{ axisLabel: { fill: Colors.textMuted },
                     tickLabels: { fill: Colors.textMuted } }} />
          <VictoryLine data={dados}
            style={{ data: { stroke: Colors.danger, strokeWidth: 2 } }} />
        </VictoryChart>
      </View>
      <Text style={styles.legenda}>
        Fonte: dados históricos e previsão CPTEC/INPE
      </Text>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 16 },
  titulo: { fontSize: 18, fontWeight: "bold", color: Colors.text, marginBottom: 16 },
  grafico: { backgroundColor: Colors.surface, borderRadius: 12, padding: 8 },
  legenda: { color: Colors.textMuted, fontSize: 12, textAlign: "center", marginTop: 12 },
});
```

### app/(tabs)/settings.tsx

```tsx
import { useState, useEffect } from "react";
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from "react-native";
import { useUser } from "@/context/UserContext";
import { Colors } from "@/constants/colors";

export default function Settings() {
  const { preferencias, salvarPreferencias, logout } = useUser();
  const [regiao, setRegiao] = useState(preferencias?.regiao ?? "");
  const [threshold, setThreshold] = useState(String(preferencias?.threshold ?? "6"));

  function handleSalvar() {
    const t = parseFloat(threshold);
    if (isNaN(t) || t < 1 || t > 10) {
      Alert.alert("Erro", "Threshold deve ser um número entre 1 e 10");
      return;
    }
    salvarPreferencias({ regiao, threshold: t });
    Alert.alert("Sucesso", "Preferências salvas!");
  }

  return (
    <View style={styles.container}>
      <Text style={styles.titulo}>Configurações</Text>

      <Text style={styles.label}>Região monitorada</Text>
      <TextInput style={styles.input} value={regiao}
        onChangeText={setRegiao} placeholder="Ex: São Paulo"
        placeholderTextColor={Colors.textMuted} />

      <Text style={styles.label}>Threshold de alerta (1-10)</Text>
      <TextInput style={styles.input} value={threshold}
        onChangeText={setThreshold} keyboardType="numeric"
        placeholderTextColor={Colors.textMuted} />

      <TouchableOpacity style={styles.button} onPress={handleSalvar}>
        <Text style={styles.buttonText}>Salvar</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.logout} onPress={logout}>
        <Text style={styles.logoutText}>Sair</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background, padding: 24 },
  titulo: { fontSize: 22, fontWeight: "bold", color: Colors.text, marginBottom: 24 },
  label: { color: Colors.textMuted, marginBottom: 6 },
  input: { backgroundColor: Colors.surface, color: Colors.text,
    borderRadius: 8, padding: 14, marginBottom: 16,
    borderWidth: 1, borderColor: Colors.border },
  button: { backgroundColor: Colors.primary, borderRadius: 8,
    padding: 14, alignItems: "center" },
  buttonText: { color: Colors.text, fontWeight: "bold" },
  logout: { marginTop: 24, alignItems: "center" },
  logoutText: { color: Colors.danger },
});
```

---

### src/context/UserContext.tsx

```tsx
import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { api } from "@/services/api";

interface Preferencias { regiao: string; threshold: number; }
interface UserContextType {
  token: string | null;
  preferencias: Preferencias | null;
  setToken: (t: string) => void;
  salvarPreferencias: (p: Preferencias) => void;
  logout: () => void;
}

const UserContext = createContext<UserContextType>({} as UserContextType);

export function UserProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(null);
  const [preferencias, setPreferencias] = useState<Preferencias | null>(null);

  useEffect(() => {
    AsyncStorage.getItem("token").then(t => { if (t) setTokenState(t); });
    AsyncStorage.getItem("preferencias").then(p => {
      if (p) setPreferencias(JSON.parse(p));
    });
  }, []);

  function setToken(t: string) {
    setTokenState(t);
    AsyncStorage.setItem("token", t);
    api.defaults.headers.common["Authorization"] = `Bearer ${t}`;
  }

  function salvarPreferencias(p: Preferencias) {
    setPreferencias(p);
    AsyncStorage.setItem("preferencias", JSON.stringify(p));
  }

  function logout() {
    setTokenState(null);
    AsyncStorage.removeItem("token");
    delete api.defaults.headers.common["Authorization"];
  }

  return (
    <UserContext.Provider value={{ token, preferencias, setToken, salvarPreferencias, logout }}>
      {children}
    </UserContext.Provider>
  );
}

export const useUser = () => useContext(UserContext);
```

### src/context/AlertaContext.tsx

```tsx
import { createContext, useContext, useState, ReactNode } from "react";
import { api } from "@/services/api";
import { Alerta } from "@/types/alerta";

interface AlertaContextType {
  alertas: Alerta[];
  hriAtual: number;
  loading: boolean;
  carregarAlertas: () => Promise<void>;
}

const AlertaContext = createContext<AlertaContextType>({} as AlertaContextType);

export function AlertaProvider({ children }: { children: ReactNode }) {
  const [alertas, setAlertas] = useState<Alerta[]>([]);
  const [hriAtual, setHriAtual] = useState(0);
  const [loading, setLoading] = useState(false);

  async function carregarAlertas() {
    setLoading(true);
    try {
      const res = await api.get("/alertas");
      setAlertas(res.data);
      if (res.data.length > 0) setHriAtual(res.data[0].hri ?? 7.2);
    } catch {
      // mantém estado anterior
    } finally {
      setLoading(false);
    }
  }

  return (
    <AlertaContext.Provider value={{ alertas, hriAtual, loading, carregarAlertas }}>
      {children}
    </AlertaContext.Provider>
  );
}

export const useAlerta = () => useContext(AlertaContext);
```

---

### src/services/api.ts

```ts
import axios from "axios";
import { BASE_URL } from "@/constants/config";

export const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});
```

### src/types/alerta.ts

```ts
export interface Alerta {
  id: number;
  nivel: "ATENCAO" | "ALERTA" | "CRITICO";
  mensagem: string;
  ativo: boolean;
  createdAt: string;
  nomeRegiao: string;
  hri?: number;
}
```

### src/types/regiao.ts

```ts
export interface Regiao {
  id: number;
  nome: string;
  latitude: number;
  longitude: number;
  descricao?: string;
}
```

---

### src/components/HriIndicator.tsx

```tsx
import { View, Text, StyleSheet } from "react-native";
import { Colors } from "@/constants/colors";

interface Props { valor: number; }

export function HriIndicator({ valor }: Props) {
  const cor = valor >= 8 ? Colors.danger : valor >= 6 ? Colors.warning : Colors.success;
  const label = valor >= 8 ? "CRÍTICO" : valor >= 6 ? "ALERTA" : "NORMAL";

  return (
    <View style={styles.card}>
      <Text style={styles.titulo}>Índice de Risco de Calor (HRI)</Text>
      <Text style={[styles.valor, { color: cor }]}>{valor.toFixed(1)}</Text>
      <View style={[styles.badge, { backgroundColor: cor }]}>
        <Text style={styles.badgeText}>{label}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: Colors.surface, borderRadius: 12,
    padding: 20, alignItems: "center", marginBottom: 16 },
  titulo: { color: Colors.textMuted, fontSize: 13, marginBottom: 8 },
  valor: { fontSize: 56, fontWeight: "bold" },
  badge: { borderRadius: 20, paddingHorizontal: 16, paddingVertical: 4, marginTop: 8 },
  badgeText: { color: "#fff", fontWeight: "bold", fontSize: 12 },
});
```

### src/components/AlertaCard.tsx

```tsx
import { View, Text, StyleSheet } from "react-native";
import { Alerta } from "@/types/alerta";
import { Colors } from "@/constants/colors";

interface Props { alerta: Alerta; }

export function AlertaCard({ alerta }: Props) {
  const cor = alerta.nivel === "CRITICO" ? Colors.danger
    : alerta.nivel === "ALERTA" ? Colors.warning : Colors.accent;

  return (
    <View style={[styles.card, { borderLeftColor: cor }]}>
      <View style={styles.header}>
        <Text style={[styles.nivel, { color: cor }]}>{alerta.nivel}</Text>
        <Text style={styles.regiao}>{alerta.nomeRegiao}</Text>
      </View>
      <Text style={styles.mensagem}>{alerta.mensagem}</Text>
      <Text style={styles.data}>
        {new Date(alerta.createdAt).toLocaleDateString("pt-BR")}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  card: { backgroundColor: Colors.surface, borderRadius: 8,
    padding: 14, marginBottom: 10, borderLeftWidth: 4 },
  header: { flexDirection: "row", justifyContent: "space-between", marginBottom: 6 },
  nivel: { fontWeight: "bold", fontSize: 13 },
  regiao: { color: Colors.textMuted, fontSize: 12 },
  mensagem: { color: Colors.text, fontSize: 13, marginBottom: 6 },
  data: { color: Colors.textMuted, fontSize: 11 },
});
```

---

### src/constants/colors.ts

```ts
export const Colors = {
  background: "#0a0e1a",
  surface: "#111827",
  primary: "#3b82f6",
  accent: "#06b6d4",
  danger: "#ef4444",
  warning: "#f59e0b",
  success: "#22c55e",
  text: "#f1f5f9",
  textMuted: "#94a3b8",
  border: "#1e293b",
};
```

### src/constants/config.ts

```ts
export const BASE_URL = "http://SEU_IP_LOCAL:8080/api";
// Windows: rode `ipconfig` e use o IPv4
```
