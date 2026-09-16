-- ============================================================
-- IPMA - Sistema de Recrutamento
-- Schema inicial (PostgreSQL 15+/18)
-- ============================================================

CREATE TABLE app_user (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(40)  NOT NULL, -- CDRH, GESTOR_RH, JURI, PORTAL, ADMIN
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    create_date     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE department (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE work_location (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    district     VARCHAR(120) NOT NULL,
    municipality VARCHAR(120) NOT NULL,
    address      VARCHAR(500)
);

CREATE TABLE workflow_template (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    offer_type  VARCHAR(60)  NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    version     INTEGER NOT NULL DEFAULT 1,
    create_date TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE workflow_stage (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    template_id  BIGINT NOT NULL REFERENCES workflow_template(id) ON DELETE CASCADE,
    sequence     INTEGER NOT NULL DEFAULT 10,
    stage_code   VARCHAR(40) NOT NULL,
    name         VARCHAR(120) NOT NULL,
    mandatory    BOOLEAN NOT NULL DEFAULT TRUE,
    conditional  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE vaga (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    vaga_code               VARCHAR(60) UNIQUE,
    offer_type              VARCHAR(60)  NOT NULL,
    job_position            VARCHAR(120) NOT NULL,
    no_of_recruitment       INTEGER NOT NULL DEFAULT 1,
    publication_date        DATE,
    deadline_date           TIMESTAMP,

    vinculo                 VARCHAR(60),
    regime                  VARCHAR(60),
    salary                  NUMERIC(12,2) DEFAULT 0,
    salary_info             VARCHAR(500),
    salary_plus             NUMERIC(12,2) DEFAULT 0,

    vacancy_relation        VARCHAR(120),
    allow_no_degree         BOOLEAN NOT NULL DEFAULT FALSE,
    vagas_deficiencia       BOOLEAN NOT NULL DEFAULT FALSE,
    requirements            TEXT,
    nationality_required    BOOLEAN NOT NULL DEFAULT TRUE,
    nivel_habilitacional    VARCHAR(60),
    descricao_habilitacao   TEXT,
    other_requirements      TEXT,
    website_description     TEXT,
    procedure_description   TEXT,
    docs_list               TEXT,

    has_pc                  BOOLEAN NOT NULL DEFAULT FALSE,
    has_ac                  BOOLEAN NOT NULL DEFAULT FALSE,
    has_eac                 BOOLEAN NOT NULL DEFAULT FALSE,

    gestor_id               BIGINT REFERENCES app_user(id),
    gestor_suplente_id      BIGINT REFERENCES app_user(id),
    manager_cdrh_id         BIGINT REFERENCES app_user(id),

    juri_pres_id            BIGINT REFERENCES app_user(id),
    juri_ve1_id             BIGINT REFERENCES app_user(id),
    juri_ve2_id             BIGINT REFERENCES app_user(id),
    juri_vs1_id             BIGINT REFERENCES app_user(id),
    juri_vs2_id             BIGINT REFERENCES app_user(id),

    work_location_id        BIGINT REFERENCES work_location(id),

    workflow_template_id    BIGINT REFERENCES workflow_template(id),
    current_stage_code      VARCHAR(40),

    state                   VARCHAR(30) NOT NULL DEFAULT 'draft', -- draft, published, running, finished, cancelled, desert
    website_published       BOOLEAN NOT NULL DEFAULT FALSE,
    publish_date            TIMESTAMP,

    create_date             TIMESTAMP NOT NULL DEFAULT now(),
    write_date              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE vaga_department (
    vaga_id       BIGINT NOT NULL REFERENCES vaga(id) ON DELETE CASCADE,
    department_id BIGINT NOT NULL REFERENCES department(id) ON DELETE CASCADE,
    PRIMARY KEY (vaga_id, department_id)
);

CREATE TABLE job_stage (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id      BIGINT NOT NULL REFERENCES vaga(id) ON DELETE CASCADE,
    stage_code  VARCHAR(40) NOT NULL,
    name        VARCHAR(120) NOT NULL,
    sequence    INTEGER NOT NULL DEFAULT 10,
    mandatory   BOOLEAN NOT NULL DEFAULT TRUE,
    conditional BOOLEAN NOT NULL DEFAULT FALSE,
    state       VARCHAR(20) NOT NULL DEFAULT 'draft', -- draft, active, completed, skipped, cancelled
    start_date  TIMESTAMP,
    end_date    TIMESTAMP,
    CONSTRAINT job_stage_unique UNIQUE (job_id, stage_code)
);

CREATE TABLE applicant (
    id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_id                   BIGINT NOT NULL REFERENCES vaga(id),

    partner_name             VARCHAR(255) NOT NULL,
    birth_date               DATE NOT NULL,
    gender                   VARCHAR(20),
    nationality              VARCHAR(120) NOT NULL,
    id_number                VARCHAR(60)  NOT NULL,
    id_nif                   VARCHAR(9)   NOT NULL,
    address                  VARCHAR(500),
    postal_code              VARCHAR(10),
    locality                 VARCHAR(120),
    municipality             VARCHAR(120),
    email_from               VARCHAR(255) NOT NULL,
    partner_phone            VARCHAR(30),
    partner_mobile           VARCHAR(30) NOT NULL,

    education_course         TEXT,
    postgrad_info            TEXT,
    public_employment        BOOLEAN NOT NULL DEFAULT FALSE,
    employment_situation     VARCHAR(255),
    last_employer            VARCHAR(255),
    last_activity            VARCHAR(255),
    performance_evaluation   VARCHAR(255),
    relevant_experience      TEXT,
    other_experience         TEXT,
    alternative_qualification TEXT,

    selection_methods        VARCHAR(20), -- AC, PC
    has_disability           BOOLEAN NOT NULL DEFAULT FALSE,
    special_needs_desc       TEXT,

    declaration_true         BOOLEAN NOT NULL DEFAULT FALSE,
    mob_dec                  BOOLEAN,

    -- Requisitos de Admissão (triagem)
    habilit_ok               BOOLEAN,
    vinculo_ok                BOOLEAN,
    docs_ok                  BOOLEAN,
    exp_ok                   BOOLEAN,
    motivo_exclusao          TEXT,
    rh_response              TEXT,

    -- Notas
    pc_grade                 NUMERIC(5,3),
    ac_grade                 NUMERIC(5,3),
    eac_grade                NUMERIC(5,3),
    final_grade              NUMERIC(5,3),

    state                    VARCHAR(30) NOT NULL DEFAULT 'submitted',
    -- submitted, under_review, admitted, excluded, under_appeal, approved, hired, rejected, cancelled
    channel                  VARCHAR(20), -- portal, email, fisico, sem_resposta

    create_date              TIMESTAMP NOT NULL DEFAULT now(),
    write_date                TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT applicant_job_nif_unique UNIQUE (job_id, id_nif)
);

CREATE TABLE appeal (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    applicant_id     BIGINT NOT NULL REFERENCES applicant(id) ON DELETE CASCADE,
    job_id           BIGINT NOT NULL REFERENCES vaga(id),
    phase            VARCHAR(60) NOT NULL, -- missing_requirements, appeal
    reception_date   TIMESTAMP NOT NULL DEFAULT now(),
    channel          VARCHAR(20) NOT NULL, -- portal, email, fisico
    allegations      TEXT,
    rh_response      TEXT,
    rh_response_date TIMESTAMP,
    state            VARCHAR(20) NOT NULL DEFAULT 'received' -- received, under_review, decided
);

CREATE TABLE attachment (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filename            VARCHAR(500) NOT NULL,
    content_type        VARCHAR(120),
    data                BYTEA NOT NULL,
    res_model           VARCHAR(60) NOT NULL, -- vaga, applicant, appeal
    res_id              BIGINT NOT NULL,
    document_type       VARCHAR(60),          -- ata_provisoria, ata_final, aviso_dre, cv, habilitacoes, ...
    is_public_document  BOOLEAN NOT NULL DEFAULT FALSE,
    upload_date         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_attachment_res ON attachment(res_model, res_id);
CREATE INDEX idx_applicant_job ON applicant(job_id);
CREATE INDEX idx_applicant_state ON applicant(state);
CREATE INDEX idx_vaga_published ON vaga(website_published, deadline_date);
CREATE INDEX idx_job_stage_job ON job_stage(job_id);
