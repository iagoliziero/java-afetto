-- =========================================================
-- AFETTO - MIGRATION V7
-- Avaliações das clínicas: nota de 1 a 5 e comentário opcional.
-- Cada usuário tem no máximo uma avaliação por clínica.
-- =========================================================

CREATE TABLE TBL_AVALIACAO (
                               id_avaliacao UUID PRIMARY KEY,

                               nota INT NOT NULL,
                               comentario VARCHAR(500),

                               data_criacao TIMESTAMP NOT NULL,
                               data_atualizacao TIMESTAMP,

                               id_usuario UUID NOT NULL,
                               id_clinica UUID NOT NULL,

                               CONSTRAINT ck_avaliacao_nota
                                   CHECK (nota BETWEEN 1 AND 5),

                               CONSTRAINT uk_avaliacao_usuario_clinica
                                   UNIQUE (id_usuario, id_clinica),

                               CONSTRAINT fk_avaliacao_usuario
                                   FOREIGN KEY (id_usuario)
                                       REFERENCES TBL_USUARIO(id_usuario),

                               CONSTRAINT fk_avaliacao_clinica
                                   FOREIGN KEY (id_clinica)
                                       REFERENCES TBL_CLINICA(id_clinica)
);
