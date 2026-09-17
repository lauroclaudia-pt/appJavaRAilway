-- ============================================================
-- Dados de referência: Locais de Trabalho do IPMA e Templates de Workflow
-- ============================================================

INSERT INTO work_location (display_name, district, municipality, address) VALUES
('Lisboa (Sede)', 'Lisboa', 'Lisboa', 'Rua C do Aeroporto, 1749-077 Lisboa'),
('Algés', 'Lisboa', 'Oeiras', 'Av. Alfredo Magalhães Ramalho, 6, 1495-165 Algés'),
('Porto', 'Porto', 'Maia', 'Centro Meteorológico para o Aeroporto do Porto, Apartado 19, 4471-905 Maia'),
('Faro', 'Faro', 'Faro', 'Centro Meteorológico para o Aeroporto de Faro, Apartado 20, 8005-217 Faro'),
('Ponta Delgada', 'R. A. Açores', 'Ponta Delgada', 'Rua Mãe de Deus, Relvão, 9500-321 Ponta Delgada'),
('Funchal', 'R. A. Madeira', 'Funchal', 'Rua do Lazareto, 37-39, 9060-019 Funchal'),
('Angra do Heroísmo', 'R. A. Açores', 'Angra do Heroísmo', 'Rua Padre Máximo, 9700-055 Angra do Heroísmo'),
('Aveiro', 'Aveiro', 'Aveiro', 'Canal das Pirâmides s/n, 3800-242 Aveiro'),
('Bragança', 'Bragança', 'Bragança', 'Bairro S. Sebastião - Rua dos Olivais, 5300-040 Bragança'),
('Viseu', 'Viseu', 'Viseu', 'Estrada do Aeródromo, 3515-342 Viseu');

-- Workflow: Procedimento Concursal (Comum / Reserva / Seleção Internacional)
INSERT INTO workflow_template (name, offer_type, active, version) VALUES
('Procedimento Concursal', 'PROCEDIMENTO_CONCURSAL_COMUM', TRUE, 1);
INSERT INTO workflow_stage (template_id, sequence, stage_code, name, mandatory, conditional) VALUES
(1, 10, 'opening', 'Abertura', TRUE, FALSE),
(1, 20, 'applications', 'Candidaturas', TRUE, FALSE),
(1, 30, 'admission', 'Verificação de Admitidos', TRUE, FALSE),
(1, 40, 'missing_requirements', 'Recolha de Requisitos em Falta', FALSE, TRUE),
(1, 50, 'evaluation', 'Avaliação PC/AC', TRUE, FALSE),
(1, 60, 'interview', 'Entrevista', FALSE, TRUE),
(1, 70, 'appeal', 'Audiência de Interessados', TRUE, FALSE),
(1, 80, 'contract', 'Contratação', TRUE, FALSE);

-- Workflow: Mobilidade (Interna / InterCarreiras)
INSERT INTO workflow_template (name, offer_type, active, version) VALUES
('Mobilidade', 'MOBILIDADE_INTERNA', TRUE, 1);
INSERT INTO workflow_stage (template_id, sequence, stage_code, name, mandatory, conditional) VALUES
(2, 10, 'opening', 'Abertura', TRUE, FALSE),
(2, 20, 'applications', 'Candidaturas', TRUE, FALSE),
(2, 30, 'admission', 'Verificação de Admitidos', TRUE, FALSE),
(2, 40, 'evaluation', 'Avaliação Curricular', FALSE, TRUE),
(2, 50, 'interview', 'Entrevista', FALSE, TRUE),
(2, 60, 'mobility', 'Mobilidade', TRUE, FALSE),
(2, 70, 'contract', 'Contratação', TRUE, FALSE);

-- Workflow: Concurso de Dirigentes
INSERT INTO workflow_template (name, offer_type, active, version) VALUES
('Concurso de Dirigentes', 'CONCURSO_DIRIGENTES', TRUE, 1);
INSERT INTO workflow_stage (template_id, sequence, stage_code, name, mandatory, conditional) VALUES
(3, 10, 'opening', 'Abertura', TRUE, FALSE),
(3, 20, 'applications', 'Candidaturas', TRUE, FALSE),
(3, 30, 'admission', 'Verificação de Requisitos de Admissão', TRUE, FALSE),
(3, 40, 'evaluation', 'Avaliação Curricular', TRUE, FALSE),
(3, 50, 'interview', 'Entrevista Pública', TRUE, FALSE),
(3, 60, 'appointment', 'Nomeação', TRUE, FALSE);

-- Workflow: Bolsas de Investigação Científica
INSERT INTO workflow_template (name, offer_type, active, version) VALUES
('Bolsa de Investigação Científica', 'BOLSA_INVESTIGACAO', TRUE, 1);
INSERT INTO workflow_stage (template_id, sequence, stage_code, name, mandatory, conditional) VALUES
(4, 10, 'opening', 'Abertura', TRUE, FALSE),
(4, 20, 'applications', 'Candidaturas', TRUE, FALSE),
(4, 30, 'admission', 'Verificação de Requisitos de Admissão', TRUE, FALSE),
(4, 40, 'evaluation', 'Avaliação PC/AC', TRUE, FALSE),
(4, 50, 'interview', 'Entrevista', FALSE, TRUE),
(4, 60, 'appeal', 'Audiência de Interessados', TRUE, FALSE),
(4, 70, 'contract', 'Contrato-Bolsa', TRUE, FALSE);
