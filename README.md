# DevShowcase API

Backend da plataforma **DevShowcase**.

🔗 **API em produção:** [https://devshowcase-api-nldk.onrender.com](https://devshowcase-api-nldk.onrender.com) · Swagger UI: [https://devshowcase-api-nldk.onrender.com/api/docs](https://devshowcase-api-nldk.onrender.com/api/docs) · Formulário de cadastro: [https://devshowcase-api-nldk.onrender.com/cadastro-usuario.html](https://devshowcase-api-nldk.onrender.com/cadastro-usuario.html)

> Hospedado no plano Free do Render — a primeira requisição após um período de inatividade pode demorar alguns segundos (cold start).

## Contexto acadêmico

| Item | Detalhe |
|---|---|
| Instituição | Universidade Aberta do Brasil (UAB) / UESPI |
| Curso | Tecnologia em Sistemas para Internet |
| Disciplina | Backend |
| Atividade | Regras avançadas, tratamento de erros e deploy em nuvem |

**Objetivo da atividade:** transformar a base do DevShowcase em uma API robusta, pronta para produção, aplicando regras de negócio na camada de serviço e realizando o deploy em um provedor de nuvem gratuito.

**Requisitos técnicos entregues:**
1. Endpoints REST implementados e testados: `POST /api/projects/:id/feedbacks` (nota 1-5 + comentário, com recálculo da nota média), `PUT /api/projects/:id/upvote` (incrementa curtidas) e `GET /api/projects` (filtro por tecnologia + paginação).
2. Camada de service com as regras de negócio (validação de relacionamentos, recálculo de nota média, incremento de upvotes), separada de controllers e repositories.
3. Tratamento global de exceções (400, 404, 409, JSON malformado) e documentação interativa via Swagger/OpenAPI em `/api/docs`.
4. Deploy em produção: PostgreSQL em nuvem (Supabase ou Render PostgreSQL), deploy contínuo a partir do GitHub no Render, credenciais via variáveis de ambiente.

## Sumário

- [Contexto acadêmico](#contexto-acadêmico)
- [Stack tecnológica](#stack-tecnológica)
- [Modelagem de domínio](#modelagem-de-domínio)
- [Arquitetura do projeto](#arquitetura-do-projeto)
- [Endpoints da API](#endpoints-da-api)
- [Documentação interativa (Swagger/OpenAPI)](#documentação-interativa-swaggeropenapi)
- [Tratamento global de erros](#tratamento-global-de-erros)
- [Deploy em produção](#deploy-em-produção)
- [Solução de problemas](#solução-de-problemas)
- [Próximas etapas](#próximas-etapas)

## Stack tecnológica

| Camada          | Tecnologia                                    |
|-----------------|------------------------------------------------|
| Runtime         | Java 21 (JDK)                                  |
| Framework HTTP  | Spring Boot 3 (Spring MVC)                     |
| ORM             | Spring Data JPA / Hibernate — PostgreSQL       |
| Banco de dados  | PostgreSQL (driver `org.postgresql`)           |
| Build           | Maven                                          |
| Containers      | Docker                                         |
| Validação       | Bean Validation (Jakarta Validation) nos DTOs  |
| Documentação    | springdoc-openapi / Swagger UI (`/api/docs`)   |
| Testes          | JUnit 5 + Spring Boot Test (MockMvc) + H2      |
| Deploy          | Render (PaaS, via Docker) + Supabase/Render PostgreSQL |

## Modelagem de domínio

4 entidades e 3 relacionamentos, conforme exigido na especificação:

```mermaid
erDiagram
    PROFILE ||--o{ PROJECT : "1:N"
    PROJECT ||--o{ FEEDBACK : "1:N"
    PROJECT }o--o{ TECHNOLOGY : "N:N"

    PROFILE {
        int id PK
        string name
        string email
        text bio
        string avatarUrl
    }
    PROJECT {
        int id PK
        string title
        text description
        string repositoryUrl
        int profileId FK
        float averageRating
        int upvotes
    }
    TECHNOLOGY {
        int id PK
        string name
    }
    FEEDBACK {
        int id PK
        text comment
        int rating
        int projectId FK
    }
```

- **Profile 1:N Project** — um perfil possui vários projetos.
- **Project N:N Technology** — um projeto usa várias tecnologias, e uma tecnologia aparece em vários projetos (tabela de junção `project_technologies`).
- **Project 1:N Feedback** — um projeto recebe vários feedbacks.

## Arquitetura do projeto

Fluxo de uma requisição, camada por camada:

```mermaid
flowchart LR
    A[Rota / Controller] --> B[Bean Validation<br/>DTO de entrada / query / path]
    B --> C[Controller]
    C --> D[Service<br/>regras de negócio]
    D --> E[Repository<br/>Spring Data JPA]
    E --> F[Entity JPA]
    F --> G[(Banco de dados)]
    C --> H[DTO de saída]
    H --> I[Resposta JSON]
    C -.erro.-> J[GlobalExceptionHandler]
    J --> I
```

```
src/main/java/com/devshowcase/api/
  config/        # CORS, OpenAPI e parsing de DATABASE_URL
  entity/        # Entidades JPA (Profile, Project, Technology, Feedback)
  repository/    # Interfaces Spring Data JPA (acesso a dados)
  dto/           # DTOs de entrada (Bean Validation) e de saída
  service/       # Regras de negócio (validação de relacionamentos, nota média, upvotes)
  controller/    # Endpoints REST (chamam services)
  exception/     # Exceções customizadas + GlobalExceptionHandler (@RestControllerAdvice)
  validation/    # Validador customizado @ValidUrl
  DevshowcaseApiApplication.java   # Ponto de entrada (main)
src/main/resources/
  application.yml        # Configuração principal (porta, datasource, springdoc)
render.yaml               # Blueprint de deploy contínuo no Render (Docker)
Dockerfile                # Build multi-stage (Maven + JDK 21 -> JRE 21)
pom.xml                    # Build Maven (dependências, plugin Spring Boot)
```

## Endpoints da API

| Método | Rota                                | Descrição                                                        |
|--------|-------------------------------------|--------------------------------------------------------------------|
| POST   | `/api/profiles`                    | Cadastra um perfil de desenvolvedor                                 |
| GET    | `/api/profiles/:id`                | Busca um perfil por id                                              |
| POST   | `/api/technologies`                | Cadastra uma tecnologia                                             |
| GET    | `/api/technologies`                | Lista todas as tecnologias                                          |
| POST   | `/api/projects`                    | Cadastra um projeto                                                 |
| GET    | `/api/projects`                    | Lista projetos com filtro por tecnologia e paginação                |
| POST   | `/api/projects/:id/feedbacks`      | Registra nota (1-5) + comentário e recalcula a nota média do projeto |
| PUT    | `/api/projects/:id/upvote`         | Incrementa em 1 as curtidas/estrelas (upvotes) do projeto            |

### Profiles

**POST /api/profiles**
```json
{
  "name": "Carlos Silva",
  "email": "carlos@example.com",
  "bio": "Dev backend apaixonado por APIs",
  "avatarUrl": "https://example.com/carlos.png"
}
```
Validações: `name` obrigatório e não vazio · `email` obrigatório, formato válido e único · `avatarUrl` opcional, deve ser URL válida.

**GET /api/profiles/:id** — retorna o perfil com a lista de projetos vinculados (404 se não existir).

### Technologies

**POST /api/technologies**
```json
{ "name": "Node.js" }
```
Validações: `name` obrigatório, não vazio e único (409 se duplicado).

**GET /api/technologies** — lista todas as tecnologias, ordenadas por nome.

### Projects

**POST /api/projects**
```json
{
  "title": "DevShowcase API",
  "description": "Backend do projeto",
  "repositoryUrl": "https://github.com/carlos/devshowcase",
  "profileId": 1,
  "technologyIds": [1, 2]
}
```
Validações: `title` obrigatório e não vazio · `repositoryUrl` obrigatória e deve ser URL válida · `profileId` obrigatório e deve referenciar um Profile existente · `technologyIds` opcional, deve referenciar Technologies existentes.

**GET /api/projects** — lista projetos de forma paginada. Aceita os parâmetros de query:
- `profileId` — filtra projetos de um perfil específico;
- `technology` — filtra projetos que usam a tecnologia informada (busca parcial, case-insensitive);
- `page` (padrão `1`) e `limit` (padrão `10`, máximo `100`) — paginação dos resultados.

Resposta:
```json
{
  "data": [ { "id": 1, "title": "...", "averageRating": 4.5, "upvotes": 3, "...": "..." } ],
  "pagination": { "page": 1, "limit": 10, "total": 1, "totalPages": 1 }
}
```

### Feedbacks

**POST /api/projects/:id/feedbacks**
```json
{ "rating": 5, "comment": "Excelente projeto!" }
```
Validações: `rating` obrigatório, inteiro entre 1 e 5 · `comment` obrigatório e não vazio · `id` do projeto deve existir (404 caso contrário).

Regra de negócio (camada de service): a cada feedback cadastrado, a nota média (`averageRating`) do projeto é recalculada a partir de todos os feedbacks existentes.

Resposta:
```json
{
  "feedback": { "id": 1, "rating": 5, "comment": "Excelente projeto!", "projectId": 1 },
  "projectAverageRating": 5
}
```

### Upvote

**PUT /api/projects/:id/upvote** — incrementa em 1 o campo `upvotes` do projeto e retorna o projeto atualizado. Responde `404` se o projeto não existir.

### Testando com `curl` (produção)

```bash
API=https://devshowcase-api-nldk.onrender.com

# Criar perfil
curl -X POST $API/api/profiles \
  -H "Content-Type: application/json" \
  -d '{"name":"Carlos Silva","email":"carlos@example.com"}'

# Buscar perfil
curl $API/api/profiles/1

# Criar tecnologia
curl -X POST $API/api/technologies \
  -H "Content-Type: application/json" -d '{"name":"Node.js"}'

# Listar tecnologias
curl $API/api/technologies

# Criar projeto
curl -X POST $API/api/projects \
  -H "Content-Type: application/json" \
  -d '{"title":"DevShowcase API","repositoryUrl":"https://github.com/carlos/devshowcase","profileId":1,"technologyIds":[1]}'

# Listar projetos (com filtro e paginação)
curl "$API/api/projects?technology=Node&page=1&limit=10"

# Registrar feedback (nota + comentário) em um projeto
curl -X POST $API/api/projects/1/feedbacks \
  -H "Content-Type: application/json" \
  -d '{"rating":5,"comment":"Excelente projeto!"}'

# Dar upvote em um projeto
curl -X PUT $API/api/projects/1/upvote
```

### Testando em produção (clique para abrir)

O deploy já está no ar em [https://devshowcase-api-nldk.onrender.com](https://devshowcase-api-nldk.onrender.com). Os links abaixo são endpoints `GET` reais, testados e funcionando — clique para abrir a resposta direto no navegador:

- [Health check — `GET /api`](https://devshowcase-api-nldk.onrender.com/api)
- [Swagger UI — documentação interativa](https://devshowcase-api-nldk.onrender.com/api/docs)
- [Formulário de cadastro de usuário](https://devshowcase-api-nldk.onrender.com/cadastro-usuario.html)
- [Listar tecnologias — `GET /api/technologies`](https://devshowcase-api-nldk.onrender.com/api/technologies)
- [Listar projetos — `GET /api/projects`](https://devshowcase-api-nldk.onrender.com/api/projects)
- [Listar projetos com filtro por tecnologia e paginação — `GET /api/projects?technology=Node&page=1&limit=5`](https://devshowcase-api-nldk.onrender.com/api/projects?technology=Node&page=1&limit=5)
- [Perfil inexistente (exemplo de erro 404) — `GET /api/profiles/999999`](https://devshowcase-api-nldk.onrender.com/api/profiles/999999)

> Endpoints `POST`/`PUT` (criar perfil, criar tecnologia, criar projeto, registrar feedback, dar upvote) não abrem só com um clique — use o [Swagger UI](https://devshowcase-api-nldk.onrender.com/api/docs) (botão "Try it out" em cada endpoint) ou os exemplos de `curl` acima.
>
> O serviço está no plano Free do Render: se ficar inativo por um tempo, a primeira requisição pode demorar ~30s (cold start) antes de responder.

## Documentação interativa (Swagger/OpenAPI)

A API expõe sua especificação OpenAPI 3.0 (gerada automaticamente pelo springdoc-openapi a partir dos controllers/DTOs anotados) através do Swagger UI, disponível em produção em:

```
https://devshowcase-api-nldk.onrender.com/api/docs
```

Ali é possível visualizar todos os endpoints, schemas de entrada/saída e executar requisições de teste diretamente pelo navegador ("Try it out").

## Tratamento global de erros

Todas as respostas de erro passam pelo tratamento central (`com.devshowcase.api.exception.GlobalExceptionHandler`, um `@RestControllerAdvice`), que traduz falhas internas em respostas JSON amigáveis e com o status HTTP correto:

| Situação | Status | Corpo da resposta |
|---|---|---|
| Validação de DTO (Bean Validation) — body, query ou path inválidos | `400` | `{ "message": "Erro de validação.", "errors": [...] }` |
| JSON malformado no corpo da requisição | `400` | `{ "message": "Corpo da requisição contém JSON inválido." }` |
| Referência inválida (ex.: `profileId`/`technologyIds` inexistentes) | `400` | `{ "message": "..." }` |
| Recurso não encontrado (perfil/projeto por id) | `404` | `{ "message": "Projeto não encontrado." }` |
| Violação de unicidade (email/tecnologia duplicados) | `409` | `{ "message": "...", "errors": [...] }` |
| Rota inexistente | `404` | `{ "message": "Rota não encontrada." }` |
| Erro inesperado do servidor | `500` | `{ "message": "Erro interno do servidor." }` |

## Deploy em produção

🔗 A API já está publicada em produção: **[https://devshowcase-api-nldk.onrender.com](https://devshowcase-api-nldk.onrender.com)** (Swagger UI em [`/api/docs`](https://devshowcase-api-nldk.onrender.com/api/docs)).

O [`render.yaml`](render.yaml) deste repositório é um **Blueprint** do Render que provisiona, em um único passo, a API web (via Docker) e o banco PostgreSQL, já conectados entre si.

### Opção A — Blueprint (API + PostgreSQL do Render, recomendado)

1. Suba este repositório no GitHub (se ainda não estiver lá).
2. No [Render Dashboard](https://dashboard.render.com), escolha **New → Blueprint** e aponte para o repositório. O Render lê o [`render.yaml`](render.yaml) e cria:
   - o banco `devshowcase-db` (plano Free, região Oregon);
   - o serviço web `devshowcase-api`, construído a partir do [`Dockerfile`](Dockerfile) (`runtime: docker`), com `DATABASE_URL` preenchida automaticamente a partir do banco criado (`fromDatabase`) e `DB_SSL=true`.
3. `autoDeploy: true` garante que cada `git push` na branch configurada dispara um novo deploy automaticamente.
4. Ao final do deploy, a API estará disponível em `https://<nome-do-serviço>.onrender.com`, com Swagger UI em `https://<nome-do-serviço>.onrender.com/api/docs`.

### Opção B — Banco no Supabase (ou PostgreSQL do Render criado manualmente)

Use esta opção se preferir manter o banco fora do Render (ex.: Supabase, que não expira após 30 dias no plano gratuito).

1. Provisione um banco PostgreSQL gratuito:
   - **[Supabase](https://supabase.com)** — crie um projeto, copie a *Connection string* (modo "Transaction" ou "Session") em Project Settings → Database.
   - **[Render PostgreSQL](https://render.com)** — crie um banco gerenciado (New → PostgreSQL) e copie a *External Connection String*.
2. Suba este repositório no GitHub e crie o serviço no Render em **New → Web Service** (sem usar o Blueprint), com **Runtime: Docker** (usa o `Dockerfile` deste repositório).
3. Configure as variáveis de ambiente de produção no dashboard do serviço (Environment):

   | Variável | Valor |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `production` |
   | `DATABASE_URL` | connection string do Supabase/Render PostgreSQL (formato `postgres://usuario:senha@host:porta/banco`) |
   | `DB_SSL` | `true` |

   O Render injeta automaticamente a variável `PORT` — o `application.yml` já resolve `server.port` a partir de `SERVER_PORT` ou `PORT`.

> Nunca commitar credenciais de banco no repositório. Na Opção A, o Render gerencia a connection string internamente (`fromDatabase`); na Opção B, `DATABASE_URL` é definida manualmente no dashboard.
>
> Um `EnvironmentPostProcessor` do Spring Boot (`com.devshowcase.api.config.DatabaseUrlEnvironmentPostProcessor`) converte `DATABASE_URL`/`DB_SSL` para `spring.datasource.url/username/password` na inicialização, permitindo configurar o banco a partir de uma única variável de ambiente.

## Solução de problemas

| Sintoma | Causa provável | Solução |
|---|---|---|
| Erro ao conectar no banco em produção | `DATABASE_URL` incorreta ou ausente nas variáveis de ambiente do serviço | Confira a *Connection string* do Supabase/Render PostgreSQL configurada em Environment no dashboard do Render |
| `GET /api/technologies` ou `/api/projects` retornam `[]`/vazio | Banco recém-criado, sem registros | Normal em um banco novo; cadastre dados via `POST` (veja exemplos de `curl` acima) |
| Erro de SSL ao conectar no Supabase/Render em produção | `DB_SSL` não definida | Defina `DB_SSL=true` nas variáveis de ambiente de produção |
| Primeira requisição demora ~30s para responder | Plano Free do Render "dorme" após inatividade (cold start) | Aguarde a primeira requisição completar; as seguintes respondem normalmente |
| Deploy falha no Render | Erro de build do Docker/Maven ou variável de ambiente ausente | Veja os logs de build/deploy no Render Dashboard do serviço `devshowcase-api` |

## Próximas etapas

- Autenticação/autorização para os endpoints de escrita.
- Cache de listagens (`GET /api/projects`) para reduzir carga no banco em produção.
- Pipeline de CI (build + testes Maven) no GitHub Actions antes do deploy automático no Render.
