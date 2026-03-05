-- Recria as views após o Hibernate gerar o schema (create-drop)
CREATE
OR REPLACE VIEW v_detalhes_imovel AS
SELECT i.id_imovel,
       i.titulo,
       i.descricao,
       CAST(i.tipo AS VARCHAR)             AS tipo,
       i.preco,
       CAST(i.genero_moradores AS VARCHAR) AS genero_moradores,
       i.aceita_animais,
       i.tem_garagem,
       i.vagas_disponiveis,
       CAST(i.status AS VARCHAR)           AS status,
       e.rua,
       e.numero                            AS num_endereco,
       e.bairro,
       e.cidade,
       e.estado,
       e.cep,
       u.nome                              AS nome_proprietario,
       u.email                             AS email_proprietario
FROM imovel i
         INNER JOIN endereco e ON i.id_imovel = e.id_imovel
         INNER JOIN usuario u ON i.id_proprietario = u.id_usuario;

CREATE
OR REPLACE VIEW v_perfil_estudante_contato AS
SELECT u.id_usuario,
       u.nome,
       u.email,
       CAST(u.genero AS VARCHAR)  AS genero,
       est.curso,
       est.instituicao,
       STRING_AGG(t.numero, ', ') AS telefones
FROM estudante est
         INNER JOIN usuario u ON est.id_estudante = u.id_usuario
         LEFT JOIN telefone t ON u.id_usuario = t.id_usuario
GROUP BY u.id_usuario,
         u.nome,
         u.email,
         u.genero,
         est.curso,
         est.instituicao;

CREATE
OR REPLACE VIEW v_relatorio_proprietario AS
SELECT u.id_usuario             AS id_proprietario,
       u.nome                   AS nome_proprietario,
       u.email                  AS email_proprietario,
       COUNT(i.id_imovel)       AS total_imoveis,
       SUM(i.vagas_disponiveis) AS total_vagas_oferecidas,
       ROUND(AVG(i.preco), 2)   AS media_preco_imoveis,
       MIN(i.preco)             AS menor_preco,
       MAX(i.preco)             AS maior_preco
FROM usuario u
         INNER JOIN imovel i ON u.id_usuario = i.id_proprietario
GROUP BY u.id_usuario,
         u.nome,
         u.email;
