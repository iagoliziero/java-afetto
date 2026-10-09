-- =========================================================
-- AFETTO - MIGRATION V9
-- Consulta agendada pelo app numa clínica parceira: o registro
-- do histórico passa a apontar para a clínica (opcional).
-- =========================================================

ALTER TABLE TBL_HISTORICO ADD COLUMN id_clinica UUID;

ALTER TABLE TBL_HISTORICO
    ADD CONSTRAINT fk_historico_clinica
        FOREIGN KEY (id_clinica)
            REFERENCES TBL_CLINICA(id_clinica);
