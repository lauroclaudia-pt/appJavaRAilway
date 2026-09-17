-- ============================================================
-- Cada responsabilidade (Gestor de RH, Júri, CDRH, Administrador) passa a ter
-- a sua PRÓPRIA janela de validade — a mesma pessoa pode ser Gestor de RH num
-- período e Júri noutro, com datas diferentes. Datas em DATE (sem hora).
-- ============================================================

ALTER TABLE trabalhador_responsabilidade DROP CONSTRAINT trabalhador_responsabilidade_pkey;

ALTER TABLE trabalhador_responsabilidade ADD COLUMN start_date DATE NOT NULL DEFAULT CURRENT_DATE;
ALTER TABLE trabalhador_responsabilidade ADD COLUMN end_date DATE;
ALTER TABLE trabalhador_responsabilidade ALTER COLUMN start_date DROP DEFAULT;
