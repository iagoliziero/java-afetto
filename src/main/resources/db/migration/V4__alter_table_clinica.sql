-- =========================================================
-- AFETTO - MIGRATION V4
-- Clínicas: descrição, site e foto para a lista e o detalhe no app
-- =========================================================

ALTER TABLE TBL_CLINICA ADD COLUMN descricao VARCHAR(500);

ALTER TABLE TBL_CLINICA ADD COLUMN site VARCHAR(255);

ALTER TABLE TBL_CLINICA ADD COLUMN imagem_url VARCHAR(500);
