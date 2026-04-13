INSERT INTO utilizador (name, email, token, password)
VALUES ('Test User 1', 'test1@example.com',
        'token1', 'OBF:1l1a1s3g1yf41xtv20731xtn1yf21s3m1kxs');
INSERT INTO utilizador (name, email, token, password)
VALUES ('Test User 2', 'test2@example.com',
        'token2', 'OBF:1l8d1s3g1yf41xtv20731xtn1yf21s3m1l4x');

INSERT INTO club (name, owner)
VALUES ('Test Club 1', 1);
INSERT INTO club (name, owner)
VALUES ('Test Club 2', 2);

INSERT INTO court (name, club)
VALUES ('Court 1', 1);
INSERT INTO court (name, club)
VALUES ('Court 2', 2);

INSERT INTO rental (date, startDuration, endDuration, utilizador, court, club)
VALUES ('2023-10-01', 10, 12, 1, 1, 1);
INSERT INTO rental (date, startDuration, endDuration, utilizador, court, club)
VALUES ('2023-10-02', 14, 16, 2, 2, 2);