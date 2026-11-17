-- FreeConnect: esquema inicial (identidad, perfiles, proyectos, propuestas y contratos).
-- Portable entre PostgreSQL y H2 (modo PostgreSQL).

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);

CREATE TABLE skills (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    category VARCHAR(60) NOT NULL
);

CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    headline VARCHAR(160),
    bio VARCHAR(2000),
    hourly_rate NUMERIC(12, 2),
    currency VARCHAR(3),
    availability VARCHAR(20),
    company_name VARCHAR(160),
    description VARCHAR(2000),
    rating_average NUMERIC(3, 2),
    rating_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE profile_skills (
    profile_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    PRIMARY KEY (profile_id, skill_id),
    CONSTRAINT fk_profile_skills_profile FOREIGN KEY (profile_id) REFERENCES profiles (id),
    CONSTRAINT fk_profile_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
);

CREATE TABLE portfolio_items (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    url VARCHAR(1000),
    description VARCHAR(1000),
    storage_key VARCHAR(500),
    CONSTRAINT fk_portfolio_profile FOREIGN KEY (profile_id) REFERENCES profiles (id)
);

CREATE INDEX idx_portfolio_profile ON portfolio_items (profile_id);

CREATE TABLE projects (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(4000),
    category VARCHAR(30) NOT NULL,
    modality VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    budget_amount NUMERIC(14, 2) NOT NULL,
    budget_currency VARCHAR(3) NOT NULL,
    deadline DATE,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_projects_client ON projects (client_id);
CREATE INDEX idx_projects_status ON projects (status);

CREATE TABLE project_skills (
    project_id UUID NOT NULL,
    skill_id UUID NOT NULL,
    PRIMARY KEY (project_id, skill_id),
    CONSTRAINT fk_project_skills_project FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE proposals (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    freelancer_id UUID NOT NULL,
    cover_letter VARCHAR(3000) NOT NULL,
    amount NUMERIC(14, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    estimated_days INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    evaluation_score INTEGER,
    evaluation_note VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_proposals_project FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE INDEX idx_proposals_project ON proposals (project_id);
CREATE INDEX idx_proposals_freelancer ON proposals (freelancer_id);

CREATE TABLE contracts (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    proposal_id UUID NOT NULL UNIQUE,
    client_id UUID NOT NULL,
    freelancer_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    scope VARCHAR(4000) NOT NULL,
    amount_amount NUMERIC(14, 2) NOT NULL,
    amount_currency VARCHAR(3) NOT NULL,
    commission_amount NUMERIC(14, 2) NOT NULL,
    commission_currency VARCHAR(3) NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_contracts_project FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_contracts_proposal FOREIGN KEY (proposal_id) REFERENCES proposals (id)
);

CREATE INDEX idx_contracts_client ON contracts (client_id);
CREATE INDEX idx_contracts_freelancer ON contracts (freelancer_id);

CREATE TABLE milestones (
    id UUID PRIMARY KEY,
    contract_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(4000),
    amount_amount NUMERIC(14, 2) NOT NULL,
    amount_currency VARCHAR(3) NOT NULL,
    due_date DATE,
    sort_order INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_milestones_contract FOREIGN KEY (contract_id) REFERENCES contracts (id)
);

CREATE INDEX idx_milestones_contract ON milestones (contract_id);

CREATE TABLE deliverables (
    id UUID PRIMARY KEY,
    milestone_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(4000),
    url VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_deliverables_milestone FOREIGN KEY (milestone_id) REFERENCES milestones (id)
);

CREATE INDEX idx_deliverables_milestone ON deliverables (milestone_id);

CREATE TABLE deliverable_tasks (
    id UUID PRIMARY KEY,
    deliverable_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    done BOOLEAN NOT NULL,
    CONSTRAINT fk_tasks_deliverable FOREIGN KEY (deliverable_id) REFERENCES deliverables (id)
);

CREATE INDEX idx_tasks_deliverable ON deliverable_tasks (deliverable_id);

CREATE TABLE deliverable_comments (
    id UUID PRIMARY KEY,
    deliverable_id UUID NOT NULL,
    author_id UUID NOT NULL,
    body VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_comments_deliverable FOREIGN KEY (deliverable_id) REFERENCES deliverables (id)
);

CREATE INDEX idx_comments_deliverable ON deliverable_comments (deliverable_id);
