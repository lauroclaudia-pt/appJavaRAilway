-- ============================================================
-- Opções configuráveis dos campos select (Administração)
-- ============================================================

CREATE TABLE select_option (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category    VARCHAR(40)  NOT NULL,
    value       VARCHAR(100) NOT NULL,
    label       VARCHAR(255) NOT NULL,
    start_date  TIMESTAMP NOT NULL DEFAULT now(),
    end_date    TIMESTAMP,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT select_option_category_value_unique UNIQUE (category, value)
);

CREATE INDEX idx_select_option_category ON select_option(category);

-- Vínculo
INSERT INTO select_option (category, value, label, sort_order) VALUES
('VINCULO', 'NOMEACAO', 'Nomeação', 10),
('VINCULO', 'NOMEACAO_TRANSITORIA', 'Nomeação Transitória', 20),
('VINCULO', 'CTFP_INDETERMINADO', 'Contrato de Trabalho em Funções Públicas por Tempo Indeterminado', 30),
('VINCULO', 'CTFP_TERMO_CERTO', 'Contrato de Trabalho em Funções Públicas a Termo Resolutivo Certo', 40),
('VINCULO', 'COMISSAO_SERVICO', 'Comissão de Serviço', 50);

-- Regime
INSERT INTO select_option (category, value, label, sort_order) VALUES
('REGIME', 'TEMPO_COMPLETO', 'Tempo Completo', 10),
('REGIME', 'TEMPO_PARCIAL', 'Tempo Parcial', 20),
('REGIME', 'SUBSTITUICAO', 'Regime de Substituição', 30);

-- Nível Habilitacional
INSERT INTO select_option (category, value, label, sort_order) VALUES
('NIVEL_HABILITACIONAL', 'ENSINO_BASICO', 'Ensino Básico', 10),
('NIVEL_HABILITACIONAL', 'ENSINO_SECUNDARIO', 'Ensino Secundário', 20),
('NIVEL_HABILITACIONAL', 'BACHARELATO', 'Bacharelato', 30),
('NIVEL_HABILITACIONAL', 'LICENCIATURA', 'Licenciatura', 40),
('NIVEL_HABILITACIONAL', 'MESTRADO', 'Mestrado', 50),
('NIVEL_HABILITACIONAL', 'DOUTORAMENTO', 'Doutoramento', 60);

-- Situação Profissional
INSERT INTO select_option (category, value, label, sort_order) VALUES
('SITUACAO_PROFISSIONAL', 'EMPREGADO', 'Empregado(a)', 10),
('SITUACAO_PROFISSIONAL', 'DESEMPREGADO', 'Desempregado(a)', 20),
('SITUACAO_PROFISSIONAL', 'TRABALHADOR_ESTUDANTE', 'Trabalhador(a)-Estudante', 30),
('SITUACAO_PROFISSIONAL', 'ESTUDANTE', 'Estudante', 40);

-- Relação de Vagas/Candidaturas
INSERT INTO select_option (category, value, label, sort_order) VALUES
('RELACAO_VAGAS_CANDIDATURAS', 'GERAL', 'Recrutamento Geral', 10),
('RELACAO_VAGAS_CANDIDATURAS', 'RESERVA', 'Reserva de Recrutamento', 20),
('RELACAO_VAGAS_CANDIDATURAS', 'RESTRITO_ORGANISMO', 'Recrutamento Restrito ao Organismo', 30);
