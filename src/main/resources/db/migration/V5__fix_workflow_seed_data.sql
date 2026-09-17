-- ============================================================
-- Correção de dados da V2:
--  1) stage_code foi semeado em minúsculas ('opening'), mas a coluna é lida como
--     StageCode (enum Java, maiúsculas) — qualquer leitura destas linhas falhava.
--  2) offer_type 'CONCURSO_DIRIGENTES' e 'BOLSA_INVESTIGACAO' não correspondem aos
--     valores reais do enum OfferType, pelo que esses templates nunca eram encontrados.
--  3) Não existia template para PROCEDIMENTO_CONCURSAL_RESERVA,
--     PROCEDIMENTO_CONCURSAL_SELECAO_INTERNACIONAL nem MOBILIDADE_INTERCARREIRAS.
-- ============================================================

UPDATE workflow_stage SET stage_code = UPPER(stage_code);

UPDATE workflow_template SET offer_type = 'PROCEDIMENTO_CONCURSAL_CARGOS_DIRECAO' WHERE offer_type = 'CONCURSO_DIRIGENTES';
UPDATE workflow_template SET offer_type = 'BOLSA_INVESTIGACAO_CIENTIFICA' WHERE offer_type = 'BOLSA_INVESTIGACAO';

-- Procedimento Concursal — Reserva de Recrutamento (mesma estrutura do Comum)
WITH t AS (
  INSERT INTO workflow_template (name, offer_type, active, version)
  VALUES ('Procedimento Concursal — Reserva de Recrutamento', 'PROCEDIMENTO_CONCURSAL_RESERVA', TRUE, 1)
  RETURNING id
)
INSERT INTO workflow_stage (template_id, sequence, stage_code, name, mandatory, conditional)
SELECT t.id, s.sequence, s.stage_code, s.name, s.mandatory, s.conditional
FROM t, (VALUES
  (10, 'OPENING', 'Abertura', TRUE, FALSE),
  (20, 'APPLICATIONS', 'Candidaturas', TRUE, FALSE),
  (30, 'ADMISSION', 'Verificação de Admitidos', TRUE, FALSE),
  (40, 'MISSING_REQUIREMENTS', 'Recolha de Requisitos em Falta', FALSE, TRUE),
  (50, 'EVALUATION', 'Avaliação PC/AC', TRUE, FALSE),
  (60, 'INTERVIEW', 'Entrevista', FALSE, TRUE),
  (70, 'APPEAL', 'Audiência de Interessados', TRUE, FALSE),
  (80, 'CONTRACT', 'Contratação', TRUE, FALSE)
) AS s(sequence, stage_code, name, mandatory, conditional);

-- Procedimento Concursal — Seleção Internacional (mesma estrutura do Comum)
WITH t AS (
  INSERT INTO workflow_template (name, offer_type, active, version)
  VALUES ('Procedimento Concursal — Seleção Internacional', 'PROCEDIMENTO_CONCURSAL_SELECAO_INTERNACIONAL', TRUE, 1)
  RETURNING id
)
INSERT INTO workflow_stage (template_id, sequence, stage_code, name, mandatory, conditional)
SELECT t.id, s.sequence, s.stage_code, s.name, s.mandatory, s.conditional
FROM t, (VALUES
  (10, 'OPENING', 'Abertura', TRUE, FALSE),
  (20, 'APPLICATIONS', 'Candidaturas', TRUE, FALSE),
  (30, 'ADMISSION', 'Verificação de Admitidos', TRUE, FALSE),
  (40, 'MISSING_REQUIREMENTS', 'Recolha de Requisitos em Falta', FALSE, TRUE),
  (50, 'EVALUATION', 'Avaliação PC/AC', TRUE, FALSE),
  (60, 'INTERVIEW', 'Entrevista', FALSE, TRUE),
  (70, 'APPEAL', 'Audiência de Interessados', TRUE, FALSE),
  (80, 'CONTRACT', 'Contratação', TRUE, FALSE)
) AS s(sequence, stage_code, name, mandatory, conditional);

-- Mobilidade Intercarreiras (mesma estrutura da Mobilidade Interna)
WITH t AS (
  INSERT INTO workflow_template (name, offer_type, active, version)
  VALUES ('Mobilidade Intercarreiras', 'MOBILIDADE_INTERCARREIRAS', TRUE, 1)
  RETURNING id
)
INSERT INTO workflow_stage (template_id, sequence, stage_code, name, mandatory, conditional)
SELECT t.id, s.sequence, s.stage_code, s.name, s.mandatory, s.conditional
FROM t, (VALUES
  (10, 'OPENING', 'Abertura', TRUE, FALSE),
  (20, 'APPLICATIONS', 'Candidaturas', TRUE, FALSE),
  (30, 'ADMISSION', 'Verificação de Admitidos', TRUE, FALSE),
  (40, 'EVALUATION', 'Avaliação Curricular', FALSE, TRUE),
  (50, 'INTERVIEW', 'Entrevista', FALSE, TRUE),
  (60, 'MOBILITY', 'Mobilidade', TRUE, FALSE),
  (70, 'CONTRACT', 'Contratação', TRUE, FALSE)
) AS s(sequence, stage_code, name, mandatory, conditional);
