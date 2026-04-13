-- Apagar schema antigo se existir
DROP SCHEMA IF EXISTS dbo CASCADE;

-- Criar schema dbo
CREATE SCHEMA dbo;

-- Tabela de utilizadores
CREATE TABLE dbo.users
(
    id                  SERIAL PRIMARY KEY,
    name                VARCHAR(255)        NOT NULL,
    email               VARCHAR(255) UNIQUE NOT NULL,
    password_validation VARCHAR(255)        NOT NULL,
    balance             INT                 NOT NULL DEFAULT 500,
    is_admin            BOOLEAN             NOT NULL DEFAULT FALSE
);

-- Tabela de tokens
CREATE TABLE dbo.tokens
(
    token_validation VARCHAR(256) PRIMARY KEY,
    user_id          INT REFERENCES dbo.users (id) ON DELETE CASCADE,
    created_at       BIGINT NOT NULL,
    last_used_at     BIGINT NOT NULL
);

-- Tabela de convites
CREATE TABLE dbo.invites
(
    code       VARCHAR(255) PRIMARY KEY,
    create_by  INT       NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used       BOOLEAN   NOT NULL DEFAULT FALSE
);

-- Tabela de lobbies
CREATE TABLE dbo.lobbies
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255) UNIQUE NOT NULL,
    description VARCHAR(511)        NOT NULL,
    max_players INT                 NOT NULL,
    host_id     INT                 NOT NULL REFERENCES dbo.users (id),
    rounds      INT                 NOT NULL,
    state       VARCHAR(20)         NOT NULL DEFAULT 'OPEN' CHECK (state IN ('OPEN', 'MATCH_IN_PROGRESS', 'FINISHED')),
    ante        INT                 NOT NULL DEFAULT 1,
    match_id    INT                 NOT NULL DEFAULT 0,
    auto_start_at BIGINT            NULL
);


CREATE TABLE dbo.lobby_players
(
    lobby_id INT NOT NULL REFERENCES dbo.lobbies (id) ON DELETE CASCADE,
    user_id  INT NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    PRIMARY KEY (lobby_id, user_id)
);

-- Tabela de matches
CREATE TABLE dbo.matches
(
    id            SERIAL PRIMARY KEY,
    lobby_id      INT         NOT NULL REFERENCES dbo.lobbies (id) ON DELETE CASCADE,
    current_round INT         NOT NULL DEFAULT 0,
    state         VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' CHECK (state IN ('IN_PROGRESS', 'COMPLETED'))
);


-- Tabela de rounds
CREATE TABLE dbo.rounds
(
    id                SERIAL PRIMARY KEY,
    match_id          INT NOT NULL REFERENCES dbo.matches (id) ON DELETE CASCADE,
    round_num         INT NOT NULL,
    current_player_id INT DEFAULT NULL REFERENCES dbo.users (id),
    UNIQUE (match_id, round_num)
);


-- Tabela de turns
CREATE TABLE dbo.turns
(
    id          SERIAL PRIMARY KEY,
    round_id    INT                               NOT NULL REFERENCES dbo.rounds (id) ON DELETE CASCADE,
    player_id   INT                               NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    dice_values INT[5] NOT NULL,
    roll_count  INT         DEFAULT 0             NOT NULL CHECK (roll_count BETWEEN 0 AND 3),
    state       VARCHAR(20) DEFAULT 'IN_PROGRESS' NOT NULL CHECK (state IN ('IN_PROGRESS', 'COMPLETED')),
    score       VARCHAR(30) DEFAULT NULL,
    UNIQUE (round_id, player_id)
);

-- Join table: match_rounds
CREATE TABLE dbo.match_rounds
(
    match_id INT NOT NULL REFERENCES dbo.matches (id) ON DELETE CASCADE,
    round_id INT NOT NULL REFERENCES dbo.rounds (id) ON DELETE CASCADE,
    PRIMARY KEY (match_id, round_id)
);

-- Join table: match_players
CREATE TABLE dbo.match_players
(
    match_id INT NOT NULL REFERENCES dbo.matches (id) ON DELETE CASCADE,
    user_id  INT NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    PRIMARY KEY (match_id, user_id)
);

-- Join table: round_turns
CREATE TABLE dbo.round_turns
(
    round_id INT NOT NULL REFERENCES dbo.rounds (id) ON DELETE CASCADE,
    turn_id  INT NOT NULL REFERENCES dbo.turns (id) ON DELETE CASCADE,
    PRIMARY KEY (round_id, turn_id)
);

CREATE TABLE dbo.round_winners
(
    round_id INT NOT NULL REFERENCES dbo.rounds (id) ON DELETE CASCADE,
    user_id  INT NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    PRIMARY KEY (round_id, user_id)
);

CREATE TABLE dbo.match_winners
(
    match_id INT NOT NULL REFERENCES dbo.matches (id) ON DELETE CASCADE,
    user_id  INT NOT NULL REFERENCES dbo.users (id) ON DELETE CASCADE,
    PRIMARY KEY (match_id, user_id)
);

CREATE TABLE dbo.user_stats
(
    user_id        INT PRIMARY KEY REFERENCES dbo.users(id) ON DELETE CASCADE,

    rounds_won     INT NOT NULL DEFAULT 0,
    rounds_lost    INT NOT NULL DEFAULT 0,
    rounds_drawn   INT NOT NULL DEFAULT 0,

    total_matches  INT NOT NULL DEFAULT 0,
    matches_won    INT NOT NULL DEFAULT 0,
    matches_lost   INT NOT NULL DEFAULT 0,
    matches_drawn  INT NOT NULL DEFAULT 0,

    winrate        DOUBLE PRECISION NOT NULL DEFAULT 0.0
);

