-- =========================================================
-- AFETTO - MIGRATION V3
-- Consultas do pet: cada consulta é um registro do histórico
-- (TBL_HISTORICO). Ganha o horário e o nome do veterinário.
-- =========================================================

ALTER TABLE TBL_HISTORICO ADD COLUMN hora TIME;

ALTER TABLE TBL_HISTORICO ADD COLUMN nome_veterinario VARCHAR(100);
