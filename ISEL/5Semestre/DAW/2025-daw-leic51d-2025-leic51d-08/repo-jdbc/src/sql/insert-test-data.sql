BEGIN;

INSERT INTO dbo.users (name, email, password_validation, balance, is_admin)
VALUES ('PaulAtreides',
        'paul@atreides.com',
        '$2a$10$voE2l/diDCqtba7fO6jYa.NMMjHjTDfck.O80G7WwlDEospa/E59i',
        500,
        TRUE);

INSERT INTO dbo.user_stats (user_id)
VALUES (1);

COMMIT;