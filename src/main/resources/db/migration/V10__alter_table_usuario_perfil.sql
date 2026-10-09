-- =========================================================
-- AFETTO - MIGRATION V10
-- Finalizar cadastro: dados da casa do tutor
-- (o endereço continua na hierarquia TBL_ENDERECO já existente)
-- =========================================================

ALTER TABLE TBL_USUARIO ADD COLUMN tipo_moradia VARCHAR(20);

ALTER TABLE TBL_USUARIO ADD COLUMN tela_protecao BOOLEAN;

ALTER TABLE TBL_USUARIO ADD COLUMN quantidade_pets INT;
