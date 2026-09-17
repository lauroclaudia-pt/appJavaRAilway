-- ============================================================
-- Separação Trabalhador (pessoa) / Login (conta de acesso):
--  - app_user passa a trabalhador; email/password_hash tornam-se opcionais
--    (um membro de júri pode não ter conta de acesso à plataforma);
--  - a antiga coluna "role" (um único valor) é substituída por uma tabela
--    trabalhador_responsabilidade (muitos-para-muitos), permitindo que um
--    trabalhador acumule várias responsabilidades (ex.: CDRH + JURI).
-- ============================================================

ALTER TABLE app_user RENAME TO trabalhador;

ALTER TABLE trabalhador ALTER COLUMN email DROP NOT NULL;
ALTER TABLE trabalhador ALTER COLUMN password_hash DROP NOT NULL;

CREATE TABLE trabalhador_responsabilidade (
    trabalhador_id    BIGINT NOT NULL REFERENCES trabalhador(id) ON DELETE CASCADE,
    responsabilidade  VARCHAR(40) NOT NULL,
    PRIMARY KEY (trabalhador_id, responsabilidade)
);

-- Migra o valor único existente de "role" para a nova tabela, antes de a remover.
INSERT INTO trabalhador_responsabilidade (trabalhador_id, responsabilidade)
SELECT id, role FROM trabalhador WHERE role IS NOT NULL;

ALTER TABLE trabalhador DROP COLUMN role;

-- Data de publicação específica na Bolsa de Emprego Público (distinta da data de
-- publicação no website, já existente).
ALTER TABLE vaga ADD COLUMN bep_publication_date DATE;
