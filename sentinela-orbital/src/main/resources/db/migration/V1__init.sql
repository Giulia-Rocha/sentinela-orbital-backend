-- ============================================================
-- Sentinela Orbital — Script inicial do banco PostgreSQL
-- Flyway: V1__init.sql
-- ============================================================

-- USUARIO
CREATE TABLE usuario (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(100)        NOT NULL,
    email      VARCHAR(150)        NOT NULL UNIQUE,
    senha      VARCHAR(255)        NOT NULL,
    created_at TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- REGIAO
CREATE TABLE regiao (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(100)        NOT NULL,
    latitude   DOUBLE PRECISION    NOT NULL,
    longitude  DOUBLE PRECISION    NOT NULL,
    descricao  VARCHAR(255),
    usuario_id BIGINT              NOT NULL,
    CONSTRAINT fk_regiao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);

-- LEITURA_CLIMATICA
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

-- ALERTA
CREATE TABLE alerta (
    id          BIGSERIAL PRIMARY KEY,
    regiao_id   BIGINT       NOT NULL,
    leitura_id  BIGINT       NOT NULL,
    nivel       VARCHAR(20)  NOT NULL CHECK (nivel IN ('ATENCAO', 'ALERTA', 'CRITICO')),
    mensagem    VARCHAR(500) NOT NULL,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_alerta_regiao   FOREIGN KEY (regiao_id)  REFERENCES regiao(id),
    CONSTRAINT fk_alerta_leitura  FOREIGN KEY (leitura_id) REFERENCES leitura_climatica(id)
);

-- ÍNDICES
CREATE INDEX idx_leitura_regiao    ON leitura_climatica(regiao_id);
CREATE INDEX idx_leitura_timestamp ON leitura_climatica(timestamp);
CREATE INDEX idx_alerta_regiao     ON alerta(regiao_id);
CREATE INDEX idx_alerta_ativo      ON alerta(ativo);
