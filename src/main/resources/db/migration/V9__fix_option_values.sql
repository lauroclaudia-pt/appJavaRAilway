-- ============================================================
-- Corrige os valores das listas para os exatos definidos no levantamento de
-- requisitos, e acrescenta a lista de Cargo/Carreira - Categoria.
-- ============================================================

DELETE FROM select_option WHERE category = 'VINCULO';
INSERT INTO select_option (category, value, label, sort_order) VALUES
('VINCULO', 'CTFP_TERMO_INCERTO', 'CTFP a termo incerto', 10),
('VINCULO', 'CTFP_TERMO_CERTO', 'CTFP a termo certo', 20),
('VINCULO', 'CTFP_INDETERMINADO', 'CTFP por tempo indeterminado', 30),
('VINCULO', 'NOMEACAO', 'Nomeação', 40),
('VINCULO', 'COMISSAO_SERVICO', 'Comissão de Serviço', 50);

DELETE FROM select_option WHERE category = 'REGIME';
INSERT INTO select_option (category, value, label, sort_order) VALUES
('REGIME', 'CARREIRAS_GERAIS', 'Carreiras Gerais', 10),
('REGIME', 'CARREIRAS_ESPECIAIS', 'Carreiras Especiais', 20),
('REGIME', 'DIRIGENTES', 'Dirigentes', 30),
('REGIME', 'OUTRA_BOLSA', 'Outra (bolsa)', 40);

DELETE FROM select_option WHERE category = 'RELACAO_VAGAS_CANDIDATURAS';
INSERT INTO select_option (category, value, label, sort_order) VALUES
('RELACAO_VAGAS_CANDIDATURAS', 'COM_RELACAO', 'Com relação jurídica de emprego público', 10),
('RELACAO_VAGAS_CANDIDATURAS', 'SEM_RELACAO', 'Sem relação jurídica de emprego público', 20);

INSERT INTO select_option (category, value, label, sort_order) VALUES
('CARGO_CARREIRA', 'DIRETOR_SERVICOS', 'Diretor de Serviços', 10),
('CARGO_CARREIRA', 'CHEFE_DIVISAO', 'Chefe de Divisão', 20),
('CARGO_CARREIRA', 'TECNICO_SUPERIOR', 'Técnico Superior', 30),
('CARGO_CARREIRA', 'ESPECIALISTA_INFORMATICA', 'Especialista Informática', 40),
('CARGO_CARREIRA', 'TECNICO_INFORMATICA', 'Técnico Informática', 50),
('CARGO_CARREIRA', 'COORDENADOR_TECNICO', 'Coordenador Técnico', 60),
('CARGO_CARREIRA', 'ASSISTENTE_TECNICO', 'Assistente Técnico', 70),
('CARGO_CARREIRA', 'ENCARREGADO_GERAL', 'Encarregado Geral', 80),
('CARGO_CARREIRA', 'ASSISTENTE_OPERACIONAL', 'Assistente Operacional', 90),
('CARGO_CARREIRA', 'INVESTIGADOR', 'Investigador', 100),
('CARGO_CARREIRA', 'MARITIMO', 'Marítimo', 110);
