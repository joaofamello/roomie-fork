# Roomie

## Plataforma de Gerenciamento de Moradias Estudantis

## Sobre o Projeto

A **Plataforma de Gerenciamento de Moradias Estudantis (roomie)** é um sistema desenvolvido para facilitar a busca,
oferta e organização de espaços de moradia entre estudantes.

A plataforma centraliza informações e conecta estudantes que procuram moradia com proprietários ou outros estudantes
interessados em dividir um imóvel. O sistema permite que estudantes criem perfis detalhados, informando seus gostos,
hobbies e etc.

Proprietários podem cadastrar imóveis com descrições e fotos.  
Como diferencial, o sistema prevê um mecanismo de correspondência inteligente para sugerir colegas de moradia
compatíveis com base em hábitos, estilo de vida e interesses em comum.

O objetivo principal é simplificar o processo de encontrar e organizar moradias estudantis, promovendo a formação de
comunidades e garantindo melhores condições de moradia durante a vida acadêmica.

## Integrantes

- [João Francisco Araújo de Mello](https://github.com/joaofamello)
- [José Gustavo Andrade da Silva](https://github.com/Gustavo7a)
- [Jurandir Tenório Vaz Neto](https://github.com/Jurandirtvaz)
- [Guilherme Henrique Barbosa de Souza Lima](https://github.com/Castlus)
- [Lucas Henrique de Andrade Silva](https://github.com/Lucasenrique-s)
- [Luigi Mateo e Silva](https://github.com/memelon220)
- [Pablo Roldão Pereira Santos](https://github.com/pablo-roldao)

## Objetivos

- Facilitar a busca por moradias estudantis
- Permitir o cadastro e gerenciamento de imóveis
- Conectar estudantes com interesses e perfis compatíveis
- Oferecer filtros avançados por localização, tipo de imóvel e preferências
- Promover melhor organização e planejamento da moradia durante a vida acadêmica

## Tecnologias Utilizadas

### Frontend

[![My Skills](https://skillicons.dev/icons?i=angular,typescript,html,css)](https://skillicons.dev)

### Backend

[![My Skills](https://skillicons.dev/icons?i=java,spring,postgres,docker)](https://skillicons.dev)

## Deploy

A aplicação está disponível nos seguintes ambientes de produção:

| Serviço     | URL                                                                                  |
|:------------|:-------------------------------------------------------------------------------------|
| Frontend    | [https://roomie-front.onrender.com](https://roomie-front.onrender.com)                 |
| Backend API | [https://roomie-dp98.onrender.com](https://roomie-dp98.onrender.com) |

---

## Como Rodar o Projeto

Este guia contém as instruções completas para configurar e rodar o ambiente de desenvolvimento localmente usando Docker.

### Pré-requisitos

Certifique-se de ter as seguintes ferramentas instaladas:

* [Git](https://git-scm.com/)
* [Docker](https://www.docker.com/) & Docker Compose

> **Nota:** Não é necessário instalar Java, Node.js ou PostgreSQL na sua máquina para *rodar* o sistema. O Docker cuida
> de tudo.

---

### 1. Clonando o Repositório

Este projeto utiliza **Git Submodules** para gerenciar o esquema do banco de dados. A clonagem deve ser feita de forma
recursiva:

```bash
# Clone o repositório principal baixando também o submodule do banco
git clone --recurse-submodules https://github.com/MocoGroup/roomie

# Entre na pasta do projeto
cd roomie

```

#### Esqueceu da flag recursiva?

Se a pasta `database` estiver vazia, execute:

```bash
git submodule update --init --recursive
```

#### Atualizando o submodule do banco de dados

Para sincronizar o submodule com a versão mais recente do repositório remoto:

```bash
git submodule update --remote --merge
```

### 2. Configurando Variáveis de Ambiente (.env)

O sistema é configurável através de um arquivo `.env` na raiz.

1. Crie um arquivo chamado `.env` na **raiz** do projeto (mesmo local do `docker-compose.yml`).
2. Copie o conteúdo abaixo e ajuste conforme necessário:

| Parâmetro     |           Valor |
|:--------------|----------------:|
| DB_USER       |     `your-user` |
| DB_PASSWORD   | `your-password` |
| DB_NAME       |       `db-name` |
| DB_PORT       |       `db_port` |
| DB_HOST       |     `localhost` |
| BACKEND_PORT  |          `8080` |
| FRONTEND_PORT |          `4200` |

***É importante que o `BACKEND_PORT` e `FRONTEND_PORT` sejam os mesmos da tabela acima***

### 3. Executando a Aplicação

Com o Docker rodando e o `.env` configurado, execute:

```bash
docker-compose up --build
```

Isso irá:

1. Subir o banco de dados ***PostgreSQL***.
2. Compilar e iniciar o ***Backend*** (Spring Boot).
3. Compilar o ***Frontend*** (Angular) e serví-lo via Nginx.

| Serviço        | URL                     |                                                                    Descrição |
|:---------------|-------------------------|-----------------------------------------------------------------------------:|
| Frontend       | `http://localhost:4200` |                                                                Aplicação Web |
| Backend API    | `http://localhost:8080` |                                                             Endpoints da API |
| Banco de Dados | `localhost`             | Host para conexão via DBeaver/PgAdmin (Use a porta que você definiu no .env) |

## Status do Projeto

Em desenvolvimento. Versão inicial disponível em produção.


