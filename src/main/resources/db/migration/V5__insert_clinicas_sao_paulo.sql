-- =========================================================
-- AFETTO - MIGRATION V5
-- Clínicas parceiras de exemplo em São Paulo (dados FICTÍCIOS para
-- a demonstração). Telefones (11) 0000-000X e sites .example são
-- de mentira de propósito: não levam a nenhum lugar real.
-- Fotos: Pexels (uso livre - https://www.pexels.com/license/).
-- =========================================================

-- Endereço: País > Estado > Cidade > Bairro > Logradouro > Endereço
INSERT INTO TBL_PAIS (id_pais, nome, sigla) VALUES ('b0000000-0000-4000-8000-000000000001', 'Brasil', 'BR');
INSERT INTO TBL_ESTADO (id_estado, nome, sigla, id_pais) VALUES ('b1000000-0000-4000-8000-000000000001', 'São Paulo', 'SP', 'b0000000-0000-4000-8000-000000000001');
INSERT INTO TBL_CIDADE (id_cidade, nome, id_estado) VALUES ('b2000000-0000-4000-8000-000000000001', 'São Paulo', 'b1000000-0000-4000-8000-000000000001');

INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000001', 'Pinheiros', 'b2000000-0000-4000-8000-000000000001');
INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000002', 'Vila Mariana', 'b2000000-0000-4000-8000-000000000001');
INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000003', 'Moema', 'b2000000-0000-4000-8000-000000000001');
INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000004', 'Tatuapé', 'b2000000-0000-4000-8000-000000000001');
INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000005', 'Santana', 'b2000000-0000-4000-8000-000000000001');
INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000006', 'Butantã', 'b2000000-0000-4000-8000-000000000001');
INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000007', 'Lapa', 'b2000000-0000-4000-8000-000000000001');
INSERT INTO TBL_BAIRRO (id_bairro, nome, id_cidade) VALUES ('b3000000-0000-4000-8000-000000000008', 'Mooca', 'b2000000-0000-4000-8000-000000000001');

INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000001', 'Rua dos Pinheiros', 'b3000000-0000-4000-8000-000000000001');
INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000002', 'Rua Domingos de Morais', 'b3000000-0000-4000-8000-000000000002');
INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000003', 'Avenida Ibirapuera', 'b3000000-0000-4000-8000-000000000003');
INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000004', 'Rua Tuiuti', 'b3000000-0000-4000-8000-000000000004');
INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000005', 'Rua Voluntários da Pátria', 'b3000000-0000-4000-8000-000000000005');
INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000006', 'Avenida Corifeu de Azevedo Marques', 'b3000000-0000-4000-8000-000000000006');
INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000007', 'Rua Guaicurus', 'b3000000-0000-4000-8000-000000000007');
INSERT INTO TBL_LOGRADOURO (id_logradouro, nome, id_bairro) VALUES ('b4000000-0000-4000-8000-000000000008', 'Rua da Mooca', 'b3000000-0000-4000-8000-000000000008');

INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000001', '850', NULL, '05422-001', -23.5660000, -46.6845000, 'b4000000-0000-4000-8000-000000000001');
INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000002', '1500', NULL, '04010-100', -23.5900000, -46.6350000, 'b4000000-0000-4000-8000-000000000002');
INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000003', '2200', 'Loja 3', '04028-002', -23.6010000, -46.6650000, 'b4000000-0000-4000-8000-000000000003');
INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000004', '1600', NULL, '03307-000', -23.5420000, -46.5760000, 'b4000000-0000-4000-8000-000000000004');
INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000005', '2700', NULL, '02401-000', -23.5000000, -46.6250000, 'b4000000-0000-4000-8000-000000000005');
INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000006', '1100', NULL, '05582-000', -23.5720000, -46.7210000, 'b4000000-0000-4000-8000-000000000006');
INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000007', '900', 'Sala 2', '05033-001', -23.5250000, -46.6960000, 'b4000000-0000-4000-8000-000000000007');
INSERT INTO TBL_ENDERECO (id_endereco, numero, complemento, cep, latitude, longitude, id_logradouro) VALUES ('b5000000-0000-4000-8000-000000000008', '2400', NULL, '03104-002', -23.5570000, -46.6020000, 'b4000000-0000-4000-8000-000000000008');

-- Clínicas
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000001', 'Clínica Veterinária Bigode e Rabo', NULL, '(11) 0000-0001', 'contato@bigodeerabo.example', 'b5000000-0000-4000-8000-000000000001',
    'Clínica geral, odontologia veterinária e vacinação. Atendimento com hora marcada.',
    'https://www.bigodeerabo.example', 'https://images.pexels.com/photos/6235113/pexels-photo-6235113.jpeg?auto=compress&cs=tinysrgb&w=800');
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000002', 'Hospital Veterinário Lua Cheia', NULL, '(11) 0000-0002', 'contato@luacheia.example', 'b5000000-0000-4000-8000-000000000002',
    'Hospital com internação, exames de imagem e equipe de plantão.',
    'https://www.luacheia.example', 'https://images.pexels.com/photos/6235241/pexels-photo-6235241.jpeg?auto=compress&cs=tinysrgb&w=800');
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000003', 'Clínica Pelo Macio', NULL, '(11) 0000-0003', 'contato@pelomacio.example', 'b5000000-0000-4000-8000-000000000003',
    'Atendimento especializado em gatos, com consultório separado e ambiente calmo.',
    'https://www.pelomacio.example', 'https://images.pexels.com/photos/6816859/pexels-photo-6816859.jpeg?auto=compress&cs=tinysrgb&w=800');
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000004', 'Clínica Veterinária Farejo', NULL, '(11) 0000-0004', 'contato@farejo.example', 'b5000000-0000-4000-8000-000000000004',
    'Clínica geral, vacinação e check-up para cães e gatos.',
    'https://www.farejo.example', 'https://images.pexels.com/photos/7469229/pexels-photo-7469229.jpeg?auto=compress&cs=tinysrgb&w=800');
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000005', 'Centro Veterinário Toca do Bicho', NULL, '(11) 0000-0005', 'contato@tocadobicho.example', 'b5000000-0000-4000-8000-000000000005',
    'Ultrassom, raio-X e exames de laboratório no mesmo lugar.',
    'https://www.tocadobicho.example', 'https://images.pexels.com/photos/6234607/pexels-photo-6234607.jpeg?auto=compress&cs=tinysrgb&w=800');
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000006', 'Clínica Veterinária Ronrom', NULL, '(11) 0000-0006', 'contato@ronrom.example', 'b5000000-0000-4000-8000-000000000006',
    'Vacinação, castração e acompanhamento de filhotes.',
    'https://www.ronrom.example', 'https://images.pexels.com/photos/6816857/pexels-photo-6816857.jpeg?auto=compress&cs=tinysrgb&w=800');
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000007', 'Vet Jardim das Patas', NULL, '(11) 0000-0007', 'contato@jardimdaspatas.example', 'b5000000-0000-4000-8000-000000000007',
    'Consultas de rotina, dermatologia e nutrição para pets.',
    'https://www.jardimdaspatas.example', 'https://images.pexels.com/photos/6235049/pexels-photo-6235049.jpeg?auto=compress&cs=tinysrgb&w=800');
INSERT INTO TBL_CLINICA (id_clinica, nome, cnpj, telefone, email, id_endereco, descricao, site, imagem_url) VALUES (
    'c1a00000-0000-4000-8000-000000000008', 'Hospital Veterinário Latido 24h', NULL, '(11) 0000-0008', 'contato@latido24h.example', 'b5000000-0000-4000-8000-000000000008',
    'Pronto atendimento 24 horas, cirurgias e UTI veterinária.',
    'https://www.latido24h.example', 'https://images.pexels.com/photos/7121954/pexels-photo-7121954.jpeg?auto=compress&cs=tinysrgb&w=800');

-- Equipe: dois veterinários por clínica
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000001', 'Dra. Camila Ferraz', 'Clínica geral', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000001', '2024-02-15', 'MANHA', TRUE, 'c1a00000-0000-4000-8000-000000000001', 'd1000000-0000-4000-8000-000000000001');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000002', 'Dr. Rafael Nunes', 'Odontologia', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000002', '2024-03-15', 'TARDE', TRUE, 'c1a00000-0000-4000-8000-000000000001', 'd1000000-0000-4000-8000-000000000002');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000003', 'Dra. Juliana Prado', 'Cardiologia', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000003', '2024-04-15', 'INTEGRAL', TRUE, 'c1a00000-0000-4000-8000-000000000002', 'd1000000-0000-4000-8000-000000000003');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000004', 'Dr. Marcos Teles', 'Ortopedia', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000004', '2024-05-15', 'TARDE', TRUE, 'c1a00000-0000-4000-8000-000000000002', 'd1000000-0000-4000-8000-000000000004');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000005', 'Dra. Beatriz Lima', 'Medicina felina', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000005', '2024-06-15', 'INTEGRAL', TRUE, 'c1a00000-0000-4000-8000-000000000003', 'd1000000-0000-4000-8000-000000000005');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000006', 'Dr. Thiago Rocha', 'Dermatologia', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000006', '2024-07-15', 'MANHA', TRUE, 'c1a00000-0000-4000-8000-000000000003', 'd1000000-0000-4000-8000-000000000006');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000007', 'Dra. Patrícia Gomes', 'Clínica geral', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000007', '2024-08-15', 'INTEGRAL', TRUE, 'c1a00000-0000-4000-8000-000000000004', 'd1000000-0000-4000-8000-000000000007');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000008', 'Dr. André Souza', 'Vacinação', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000008', '2024-09-15', 'MANHA', TRUE, 'c1a00000-0000-4000-8000-000000000004', 'd1000000-0000-4000-8000-000000000008');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000009', 'Dra. Larissa Melo', 'Diagnóstico por imagem', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000009', '2024-01-15', 'MANHA', TRUE, 'c1a00000-0000-4000-8000-000000000005', 'd1000000-0000-4000-8000-000000000009');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000010', 'Dr. Felipe Castro', 'Clínica geral', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000010', '2024-02-15', 'TARDE', TRUE, 'c1a00000-0000-4000-8000-000000000005', 'd1000000-0000-4000-8000-000000000010');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000011', 'Dra. Renata Alves', 'Reprodução e castração', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000011', '2024-03-15', 'TARDE', TRUE, 'c1a00000-0000-4000-8000-000000000006', 'd1000000-0000-4000-8000-000000000011');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000012', 'Dr. Bruno Carvalho', 'Clínica geral', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000012', '2024-04-15', 'MANHA', TRUE, 'c1a00000-0000-4000-8000-000000000006', 'd1000000-0000-4000-8000-000000000012');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000013', 'Dra. Mariana Duarte', 'Dermatologia', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000013', '2024-05-15', 'INTEGRAL', TRUE, 'c1a00000-0000-4000-8000-000000000007', 'd1000000-0000-4000-8000-000000000013');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000014', 'Dr. Henrique Moraes', 'Nutrição', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000014', '2024-06-15', 'TARDE', TRUE, 'c1a00000-0000-4000-8000-000000000007', 'd1000000-0000-4000-8000-000000000014');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000015', 'Dra. Fernanda Costa', 'Emergência', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000015', '2024-07-15', 'NOITE', TRUE, 'c1a00000-0000-4000-8000-000000000008', 'd1000000-0000-4000-8000-000000000015');
INSERT INTO TBL_VETERINARIO (id_veterinario, nome, especialidade, crmv, telefone) VALUES ('d1000000-0000-4000-8000-000000000016', 'Dr. Ricardo Martins', 'Cirurgia', NULL, NULL);
INSERT INTO TBL_CLINICA_VETERINARIO (id, data_inicio, turno, ativo, id_clinica, id_veterinario) VALUES ('d2000000-0000-4000-8000-000000000016', '2024-08-15', 'INTEGRAL', TRUE, 'c1a00000-0000-4000-8000-000000000008', 'd1000000-0000-4000-8000-000000000016');
