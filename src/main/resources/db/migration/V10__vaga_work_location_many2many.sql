-- ============================================================
-- Local de trabalho passa a permitir um ou mais locais por vaga (secção 18).
-- ============================================================

CREATE TABLE vaga_work_location (
    vaga_id          BIGINT NOT NULL REFERENCES vaga(id) ON DELETE CASCADE,
    work_location_id BIGINT NOT NULL REFERENCES work_location(id) ON DELETE CASCADE,
    PRIMARY KEY (vaga_id, work_location_id)
);

-- Migra os dados existentes (Many2one) para a nova tabela Many2many.
INSERT INTO vaga_work_location (vaga_id, work_location_id)
SELECT id, work_location_id FROM vaga WHERE work_location_id IS NOT NULL;

ALTER TABLE vaga DROP COLUMN work_location_id;
