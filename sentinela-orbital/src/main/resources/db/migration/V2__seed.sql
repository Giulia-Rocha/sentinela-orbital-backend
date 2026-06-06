-- Usuario de teste
INSERT INTO usuario (nome, email, senha, created_at)
VALUES ('Giulia Teste', 'giulia@teste.com',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnBPMfJf4K2', -- senha: senha123
        NOW());

-- Regiões
INSERT INTO regiao (nome, latitude, longitude, descricao, usuario_id)
VALUES
    ('Centro de São Paulo',   -23.5505, -46.6333, 'Alta densidade urbana, pouca vegetação', 1),
    ('Zona Leste - SP',       -23.5489, -46.4761, 'Região periférica com histórico de calor', 1),
    ('Campinas - Centro',     -22.9056, -47.0608, 'Centro urbano de Campinas', 1);

-- Leituras climáticas
INSERT INTO leitura_climatica (regiao_id, temperatura, umidade, iuv, hri, fonte, timestamp)
VALUES
    (1, 42.0, 18.0, 11.0, 9.2, 'MOCK', NOW() - INTERVAL '2 hours'),
    (1, 38.5, 25.0,  9.0, 7.8, 'MOCK', NOW() - INTERVAL '1 hour'),
    (2, 35.0, 40.0,  7.0, 6.1, 'MOCK', NOW() - INTERVAL '3 hours'),
    (3, 28.0, 60.0,  4.0, 3.9, 'MOCK', NOW() - INTERVAL '4 hours');

-- Alertas
INSERT INTO alerta (regiao_id, leitura_id, nivel, mensagem, ativo, created_at)
VALUES
    (1, 1, 'CRITICO',  'ALERTA CRÍTICO: Temperatura 42.0°C, HRI 9.2. Risco extremo de onda de calor.', true,  NOW() - INTERVAL '2 hours'),
    (1, 2, 'ALERTA',   'ALERTA: Temperatura 38.5°C, HRI 7.8. Condições de calor intenso.',             true,  NOW() - INTERVAL '1 hour'),
    (2, 3, 'ATENCAO',  'ATENÇÃO: Temperatura 35.0°C, HRI 6.1. Monitoramento ativo.',                   true,  NOW() - INTERVAL '3 hours'),
    (3, 4, 'ATENCAO',  'ATENÇÃO: Temperatura 28.0°C, HRI 3.9. Monitoramento ativo.',                   false, NOW() - INTERVAL '4 hours');