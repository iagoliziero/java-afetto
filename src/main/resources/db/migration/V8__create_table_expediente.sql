-- =========================================================
-- AFETTO - MIGRATION V8
-- Horário de funcionamento de cada clínica, por dia da semana.
-- É dele que saem os horários livres para agendar (de 30 em 30 min).
-- dia_semana usa os nomes do java.time.DayOfWeek (MONDAY ... SUNDAY).
-- =========================================================

CREATE TABLE TBL_EXPEDIENTE (
                                id_expediente UUID PRIMARY KEY,

                                dia_semana VARCHAR(10) NOT NULL,
                                hora_abertura TIME NOT NULL,
                                hora_fechamento TIME NOT NULL,

                                id_clinica UUID NOT NULL,

                                CONSTRAINT uk_expediente_clinica_dia
                                    UNIQUE (id_clinica, dia_semana),

                                CONSTRAINT ck_expediente_horario
                                    CHECK (hora_fechamento > hora_abertura),

                                CONSTRAINT fk_expediente_clinica
                                    FOREIGN KEY (id_clinica)
                                        REFERENCES TBL_CLINICA(id_clinica)
);

-- Clínicas 1 a 7: segunda a sexta das 8h às 18h, sábado das 8h às 12h
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000001', 'MONDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000001');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000002', 'TUESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000001');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000003', 'WEDNESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000001');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000004', 'THURSDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000001');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000005', 'FRIDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000001');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000006', 'SATURDAY', '08:00:00', '12:00:00', 'c1a00000-0000-4000-8000-000000000001');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000007', 'MONDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000002');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000008', 'TUESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000002');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000009', 'WEDNESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000002');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000010', 'THURSDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000002');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000011', 'FRIDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000002');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000012', 'SATURDAY', '08:00:00', '12:00:00', 'c1a00000-0000-4000-8000-000000000002');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000013', 'MONDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000003');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000014', 'TUESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000003');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000015', 'WEDNESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000003');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000016', 'THURSDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000003');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000017', 'FRIDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000003');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000018', 'SATURDAY', '08:00:00', '12:00:00', 'c1a00000-0000-4000-8000-000000000003');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000019', 'MONDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000004');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000020', 'TUESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000004');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000021', 'WEDNESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000004');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000022', 'THURSDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000004');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000023', 'FRIDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000004');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000024', 'SATURDAY', '08:00:00', '12:00:00', 'c1a00000-0000-4000-8000-000000000004');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000025', 'MONDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000005');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000026', 'TUESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000005');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000027', 'WEDNESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000005');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000028', 'THURSDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000005');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000029', 'FRIDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000005');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000030', 'SATURDAY', '08:00:00', '12:00:00', 'c1a00000-0000-4000-8000-000000000005');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000031', 'MONDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000006');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000032', 'TUESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000006');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000033', 'WEDNESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000006');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000034', 'THURSDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000006');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000035', 'FRIDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000006');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000036', 'SATURDAY', '08:00:00', '12:00:00', 'c1a00000-0000-4000-8000-000000000006');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000037', 'MONDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000007');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000038', 'TUESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000007');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000039', 'WEDNESDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000007');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000040', 'THURSDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000007');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000041', 'FRIDAY', '08:00:00', '18:00:00', 'c1a00000-0000-4000-8000-000000000007');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000042', 'SATURDAY', '08:00:00', '12:00:00', 'c1a00000-0000-4000-8000-000000000007');

-- Clínica 8 (Latido 24h): todos os dias, o dia inteiro
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000043', 'MONDAY', '00:00:00', '23:59:00', 'c1a00000-0000-4000-8000-000000000008');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000044', 'TUESDAY', '00:00:00', '23:59:00', 'c1a00000-0000-4000-8000-000000000008');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000045', 'WEDNESDAY', '00:00:00', '23:59:00', 'c1a00000-0000-4000-8000-000000000008');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000046', 'THURSDAY', '00:00:00', '23:59:00', 'c1a00000-0000-4000-8000-000000000008');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000047', 'FRIDAY', '00:00:00', '23:59:00', 'c1a00000-0000-4000-8000-000000000008');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000048', 'SATURDAY', '00:00:00', '23:59:00', 'c1a00000-0000-4000-8000-000000000008');
INSERT INTO TBL_EXPEDIENTE (id_expediente, dia_semana, hora_abertura, hora_fechamento, id_clinica) VALUES ('e1000000-0000-4000-8000-000000000049', 'SUNDAY', '00:00:00', '23:59:00', 'c1a00000-0000-4000-8000-000000000008');
