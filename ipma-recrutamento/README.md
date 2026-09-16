# IPMA — Sistema de Recrutamento (Java + PostgreSQL)

Reimplementação em **Java 21 / Spring Boot 3 / PostgreSQL**, sem qualquer dependência
do Odoo, com base no levantamento de requisitos funcionais do IPMA, I.P.
(`Requisitos_Recrutamento_Odoo_IPMA_v1_3.docx`) — as referências ao Odoo foram
substituídas por implementações nativas equivalentes.

## O que está implementado (v1.0)

| Área da spec | Implementação |
|---|---|
| Motor de Pipeline Dinâmico (secção 2.5) | `WorkflowTemplate` / `WorkflowStage` / `JobStage` + `WorkflowEngineService` — cria e transita o pipeline por vaga em função do tipo de oferta |
| Abertura de Vaga (secção 3) | `Vaga` (entidade) + `VagaService` + `BackOfficeVagaController` |
| Website público (secção 4) | `PublicJobController` — listagem/detalhe de ofertas publicadas, filtros por prazo |
| Formulário de candidatura (secções 5, 16.1.4) | `PortalController#apply` — validação de NIF, maioridade, duplicados, declarações condicionais, uploads |
| Validação de NIF (secção 15.1) | `NifValidationService` — algoritmo Módulo 11 nativo (substitui `stdnum`) |
| Verificação de Admitidos / Triagem (secção 6) | `ApplicantService#triagem`, `BackOfficeApplicantController` |
| Recolha de Requisitos em Falta / Alegações (secções 7, 10) | `Appeal`, `AppealService` — preserva o histórico de exclusão (estado `under_appeal`) |
| Geração de Atas em PDF (secção 15.5) | `AtaPdfService` (Apache PDFBox) — substitui os relatórios QWeb do Odoo |
| Notificações por email (secção 15.4) | `NotificationService` — templates de confirmação, exclusão provisória e admissão |
| Segurança e perfis (secção 13) | `SecurityConfig` — perfis `ADMIN`, `CDRH`, `GESTOR_RH`, `JURI`, `PORTAL` |

## Roadmap (não incluído nesta versão)

- Dashboards/KPIs (secção 12) — recomenda-se implementar com consultas SQL diretas + um
  frontend de gráficos (ex.: Recharts), já que a lógica de negócio (contagens, funil) está
  disponível via `ApplicantRepository`/`JobStageRepository`.
- Regras de registo (record rules) equivalentes às do Odoo (isolamento fino Gestor RH ⇄
  vagas próprias, Júri ⇄ vagas onde participa) — atualmente a segurança é por perfil
  (role-based); o isolamento por registo deve ser adicionado nos serviços com filtros
  `WHERE gestor_id = :currentUser OR juri_pres_id = :currentUser` etc.
- Assinatura digital / upload de documentos assinados (Cartão de Cidadão).
- Paginação real de PDFs longos na `AtaPdfService` (atualmente otimizado para listas
  moderadas de candidatos).
- Autenticação evoluída para OAuth2/JWT (atual: HTTP Basic sobre HTTPS).

## Arquitetura

```
pt.ipma.recrutamento
 ├── domain/            Entidades JPA (Vaga, Applicant, Appeal, JobStage, ...)
 │    └── enums/        OfferType, StageCode, StageState, ApplicantState, Role, ...
 ├── repository/        Spring Data JPA
 ├── service/           Regras de negócio (WorkflowEngineService, NifValidationService, ...)
 ├── web/               Controladores REST (Public / Portal / BackOffice / Admin)
 │    ├── dto/
 │    └── exception/
 └── config/            Segurança, bootstrap do utilizador admin
```

Base de dados: PostgreSQL, com schema gerido por **Flyway**
(`src/main/resources/db/migration`).

## Deploy no Railway

### 1. Criar o projeto no Railway

```bash
railway login
railway init
```

### 2. Adicionar o plugin PostgreSQL

No dashboard do Railway: **New → Database → PostgreSQL** (versão 18 disponível
diretamente no catálogo de plugins). O Railway injeta automaticamente as variáveis
`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` no serviço da aplicação
quando os dois serviços estão ligados no mesmo projeto — o `application.yml` já as lê.

### 3. Configurar variáveis de ambiente do serviço da aplicação

| Variável | Obrigatória | Descrição |
|---|---|---|
| `ADMIN_EMAIL` | Recomendado | Email do utilizador administrador inicial |
| `ADMIN_PASSWORD` | Recomendado | Password do administrador inicial (se omitida, usa `ChangeMe123!` — mudar imediatamente) |
| `MAIL_ENABLED` | Não | `true` para enviar emails reais (default `false`, apenas regista em log) |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USER` / `SMTP_PASSWORD` | Se `MAIL_ENABLED=true` | Credenciais SMTP |
| `MAIL_FROM` | Não | Endereço de envio (default `recrutamento@ipma.pt`) |
| `PORTAL_BASE_URL` | Recomendado | URL pública do serviço (ex.: `https://ipma-recrutamento.up.railway.app`) — usada nos links dos emails |

### 4. Deploy

```bash
railway up
```

O Railway deteta o `Dockerfile` na raiz do repositório (ver `railway.json`) e constrói
a imagem automaticamente. O healthcheck usa `/actuator/health`.

### 5. Primeiro acesso

Após o primeiro arranque, autentique-se em `/api/admin/users` com o email/password
definidos em `ADMIN_EMAIL`/`ADMIN_PASSWORD` (HTTP Basic Auth) para criar os utilizadores
Gestor de RH, Júri e CDRH necessários à operação normal.

## Desenvolvimento local

Requer Java 21 e um PostgreSQL local (ou `docker run -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:18`):

```bash
export PGHOST=localhost PGPORT=5432 PGDATABASE=ipma_recrutamento PGUSER=postgres PGPASSWORD=postgres
mvn spring-boot:run
```

A aplicação corre em `http://localhost:8080`. As migrações Flyway (`V1`, `V2`) criam o
schema e os dados de referência (locais de trabalho do IPMA, templates de workflow) no
primeiro arranque.

## Principais endpoints REST

| Método | Endpoint | Acesso | Descrição |
|---|---|---|---|
| GET | `/api/public/jobs` | anónimo | Ofertas publicadas |
| GET | `/api/public/jobs/{id}` | anónimo | Detalhe da oferta |
| POST | `/api/public/jobs/{id}/apply` | anónimo | Submissão de candidatura (multipart) |
| GET | `/api/portal/applicants/{id}` | PORTAL | Acompanhamento da candidatura |
| POST | `/api/portal/applicants/{id}/appeal` | PORTAL | Submeter alegação/resposta |
| POST | `/api/backoffice/vagas` | GESTOR_RH/CDRH | Criar vaga |
| POST | `/api/backoffice/vagas/{id}/publish` | GESTOR_RH/CDRH | Publicar no website |
| POST | `/api/backoffice/candidatos/{id}/triagem` | GESTOR_RH/JURI/CDRH | Triagem de requisitos |
| POST | `/api/backoffice/vagas/{id}/concluir-triagem` | GESTOR_RH/CDRH | Botão "Concluir Triagem Provisória" |
| GET | `/api/backoffice/vagas/{id}/ata` | GESTOR_RH/JURI/CDRH | Gera a Ata em PDF |
| POST | `/api/admin/users` | ADMIN | Criar Gestor RH / Júri / CDRH |
