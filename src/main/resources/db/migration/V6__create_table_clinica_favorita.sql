-- =========================================================
-- AFETTO - MIGRATION V6
-- Clínicas favoritas de cada usuário
-- =========================================================

CREATE TABLE TBL_CLINICA_FAVORITA (
                                      id_favorita UUID PRIMARY KEY,

                                      data_criacao TIMESTAMP NOT NULL,

                                      id_usuario UUID NOT NULL,
                                      id_clinica UUID NOT NULL,

                                      CONSTRAINT uk_clinica_favorita
                                          UNIQUE (id_usuario, id_clinica),

                                      CONSTRAINT fk_favorita_usuario
                                          FOREIGN KEY (id_usuario)
                                              REFERENCES TBL_USUARIO(id_usuario),

                                      CONSTRAINT fk_favorita_clinica
                                          FOREIGN KEY (id_clinica)
                                              REFERENCES TBL_CLINICA(id_clinica)
);
