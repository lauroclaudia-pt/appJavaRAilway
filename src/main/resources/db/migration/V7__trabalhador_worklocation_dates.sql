-- ============================================================
-- Estado calculado por janela de validade (Ativo/Inativo), consistente com o
-- que já existia em select_option, agora também em trabalhador e work_location.
-- ============================================================

ALTER TABLE trabalhador ADD COLUMN start_date TIMESTAMP;
ALTER TABLE trabalhador ADD COLUMN end_date TIMESTAMP;

-- Preserva o estado efetivo dos registos existentes: quem estava ativo mantém-se
-- ativo (sem data de fim); quem estava inativo passa a ter data de fim no passado.
UPDATE trabalhador SET start_date = create_date;
UPDATE trabalhador SET end_date = now() - INTERVAL '1 day' WHERE active = FALSE;

ALTER TABLE trabalhador ALTER COLUMN start_date SET NOT NULL;
ALTER TABLE trabalhador DROP COLUMN active;

ALTER TABLE work_location ADD COLUMN start_date TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE work_location ADD COLUMN end_date TIMESTAMP;
