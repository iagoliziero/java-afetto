-- =========================================================
-- AFETTO - MIGRATION V2
-- Remédios do pet (aba Cuidados: vacina, remédio, consulta)
-- =========================================================


-- =========================================================
-- REMÉDIO
-- =========================================================

CREATE TABLE TBL_REMEDIO (
                             id_remedio UUID PRIMARY KEY,

                             nome_remedio VARCHAR(100) NOT NULL,
                             dosagem VARCHAR(100),
                             frequencia VARCHAR(100),

                             data_inicio DATE NOT NULL,
                             data_fim DATE,

                             observacoes TEXT,

                             id_pet UUID NOT NULL,

                             CONSTRAINT fk_remedio_pet
                                 FOREIGN KEY (id_pet)
                                     REFERENCES TBL_PET(id)
);
