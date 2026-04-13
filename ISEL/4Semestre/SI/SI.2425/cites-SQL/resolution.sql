/*
 *   ISEL-DEETC-SisInf
 *   ND 2022-2025
 *
 *   
 *   Information Systems Project - Active Databases
 *   
 */

/* ### DO NOT REMOVE THE QUESTION MARKERS ### */


-- region Question 1.a 
CREATE or Replace Function trg_check_scooter_in_dock() 
RETURNS trigger as $$
BEGIN
	IF NOT EXISTS (
		SELECT 1 FROM DOCK d
		WHERE d.scooter = NEW.scooter and d.state = 'occupy'
	) THEN
		RAISE EXCEPTION 'Scooter % must be in a dock (state = occupy) to start a travel.', NEW.scooter;
	END IF;
	RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER check_scooter_in_dock
BEFORE INSERT ON TRAVEL
FOR EACH ROW
EXECUTE FUNCTION trg_check_scooter_in_dock();
-- endregion

-- region Question 1.b
CREATE OR REPLACE FUNCTION trg_check_unique_trip()
RETURNS trigger as $$
BEGIN
	IF EXISTS(
		SELECT 1 FROM TRAVEL t
		WHERE (t.scooter = NEW.scooter AND t.dfinal IS NULL)
	) THEN
		Raise Exception 'Scooter % is already in an ongoing trip', NEW.scooter;
	END IF;

	IF EXISTS(
		SELECT 1 FROM TRAVEL t
		WHERE (t.client = NEW.client AND t.dfinal IS NULL)
	) THEN
		RAISE EXCEPTION 'Client % is already in an ongoing trip', NEW.client;
	END IF;

	RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER check_unique_trip
BEFORE INSERT ON TRAVEL
FOR EACH ROW
EXECUTE FUNCTION trg_check_unique_trip();

-- endregion

-- region Question 2
CREATE OR REPLACE FUNCTION fx_dock_occupancy(stationid integer) 
RETURNS NUMERIC AS $$
DECLARE
    total_docks INTEGER;
    occupied_docks INTEGER;
    occupancy_rate NUMERIC;
BEGIN
    SELECT COUNT(*) INTO total_docks
    FROM DOCK
    WHERE station = stationid;
    
    IF total_docks = 0 THEN
        RETURN 0.00;
    END IF;
   
    SELECT COUNT(*) INTO occupied_docks
    FROM DOCK
    WHERE station = stationid AND state = 'occupy';
    
    occupancy_rate := occupied_docks::NUMERIC / total_docks::NUMERIC;
	
	IF (occupancy_rate> 1 or occupancy_rate<0) 
	THEN RAISE EXCEPTION 'Something wrong happened, occupacy rate = %', occupancy_rate;
	END IF;
    
    RETURN occupancy_rate;
END;
$$ LANGUAGE plpgsql;

--SELECT fx_dock_occupancy(1);--0.333(3)
--SELECT fx_dock_occupancy(2);--0.5
--SELECT fx_dock_occupancy(3);--0.5
--SELECT fx_dock_occupancy(4);--0.5
--SELECT fx_dock_occupancy(5);--0
-- endregion
 
-- region Question 3
CREATE OR REPLACE VIEW RIDER
AS
SELECT p.*,c.dtregister,cd.id AS cardid,cd.credit,cd.typeofcard
FROM CLIENT c INNER JOIN PERSON p ON (c.person=p.id)
	INNER JOIN CARD cd ON (cd.client = c.person);

CREATE OR REPLACE RULE rider_insert AS
ON INSERT TO RIDER DO INSTEAD(
	INSERT INTO PERSON (email, taxnumber, name)
	VALUES (NEW.email, NEW.taxnumber, NEW.name);

	INSERT INTO CLIENT(person, dtregister)
	VALUES (
		(SELECT id FROM PERSON where taxnumber = NEW.taxnumber),
		 NEW.dtregister--CURRENT_TIMESTAMP
	);

	INSERT INTO CARD (credit, typeofcard, client)
	VALUES(
		NEW.credit,
		NEW.typeofcard,
		(SELECT id FROM PERSON where taxnumber = NEW.taxnumber)
	);
);

CREATE OR REPLACE RULE rider_update AS
ON UPDATE TO RIDER DO INSTEAD(
	UPDATE PERSON
	SET email = NEW.email,
		name = NEW.name
	WHERE id = OLD.id;

	UPDATE CLIENT
	SET dtregister = NEW.dtregister
	WHERE person = OLD.id;

	UPDATE CARD 
	SET credit = NEW.credit,
		typeofcard = NEW.typeofcard
	WHERE client = OLD.id;
);
/*
INSERT INTO RIDER (email, taxnumber, name, dtregister, credit, typeofcard)
VALUES ('test@example.com', 111111111, 'Test', '2025-01-01 10:00:00', 10.00, 'resident');

SELECT * FROM RIDER WHERE email = 'test@example.com';
SELECT * FROM PERSON WHERE email = 'test@example.com';
SELECT * FROM CLIENT WHERE person = (SELECT id FROM PERSON WHERE email = 'test@example.com');
SELECT * FROM CARD WHERE client = (SELECT id FROM PERSON WHERE email = 'test@example.com');

INSERT INTO RIDER (email, taxnumber, name, typeofcard)
VALUES ('test2@example.com', 222222222, 'Test 2', 'tourist');

SELECT * FROM RIDER WHERE email = 'test2@example.com';

UPDATE RIDER 
SET email = 'updated1@example.com',
    name = 'Updated Test',
    taxnumber = 999999999,
    credit = 15.00,
    typeofcard = 'tourist'
WHERE email = 'test@example.com';

SELECT * FROM RIDER WHERE email = 'updated1@example.com';
SELECT * FROM PERSON WHERE email = 'updated1@example.com';
SELECT * FROM CLIENT WHERE person = (SELECT id FROM PERSON WHERE email = 'updated1@example.com');
SELECT * FROM CARD WHERE client = (SELECT id FROM PERSON WHERE email = 'updated1@example.com');

UPDATE RIDER 
SET credit = 20.00,
    name = 'Update Test 2'
WHERE email = 'test2@example.com';

SELECT * FROM RIDER WHERE email = 'test2@example.com';
*/
-- endregion

-- region Question 4
CREATE OR REPLACE PROCEDURE startTrip(dockid integer, clientid integer)
LANGUAGE plpgsql
AS $$
DECLARE
    scooter_id INTEGER;
    station_id INTEGER;
    client_card_id INTEGER;
    client_card_credit NUMERIC(4,2);
    unlock_cost NUMERIC(3,2);
BEGIN

    SELECT d.scooter, d.station INTO scooter_id, station_id
    FROM DOCK d
    WHERE d.number = dockid AND d.state = 'occupy';
    
    IF scooter_id IS NULL THEN
		RAISE EXCEPTION 'Dock % is not occupied or does not exist', dockid;
	END IF;
    
    SELECT cd.id, cd.credit INTO client_card_id, client_card_credit
    FROM CARD cd
    WHERE cd.client = clientid;
    
    IF client_card_id IS NULL THEN
        RAISE EXCEPTION 'Client % does not have a valid card', clientid;
    END IF;

	IF (client_card_credit IS NULL OR client_card_credit = 0) THEN
		RAISE EXCEPTION 'Client % does not have enough credit', clientid;
    END IF;
 
    SELECT unlock INTO unlock_cost FROM SERVICECOST;
    
    IF client_card_credit < unlock_cost THEN
        RAISE EXCEPTION 'Insufficient credit (%) to start trip (unlock cost: %)', 
              client_card_credit, unlock_cost;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM TRAVEL 
        WHERE client = clientid AND dfinal IS NULL
    ) THEN
        RAISE EXCEPTION 'Client % already has an ongoing trip', clientid;
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM TRAVEL 
        WHERE scooter = scooter_id AND dfinal IS NULL
    ) THEN
        RAISE EXCEPTION 'Scooter % is already in use', scooter_id;
    END IF;
    
    INSERT INTO TRAVEL (
        dinitial,
        client,
        scooter,
        stinitial,
        comment,
        evaluation,
        dfinal,
        stfinal
    ) VALUES (
        CURRENT_TIMESTAMP,
        clientid,
        scooter_id,
        station_id,
        NULL,
        NULL,
        NULL,
        NULL
    );
    
    UPDATE DOCK 
    SET state = 'free', scooter = NULL
    WHERE number = dockid;
    
    UPDATE CARD
    SET credit = credit - unlock_cost
    WHERE id = client_card_id;
    
    RAISE NOTICE 'Trip started';
END;
$$;
-- Novo proceimento criado para terminar a viagem (opção nossa)
CREATE OR REPLACE PROCEDURE endTrip(scooter_id integer, comment_msg text, evaluation_client integer)
LANGUAGE plpgsql
AS $$
BEGIN
	IF NOT EXISTS (
        SELECT 1 FROM TRAVEL WHERE (scooter = scooter_id AND dfinal IS NULL)
    ) THEN
        RAISE EXCEPTION 'No ongoing trip found with id %', scooter_id;
    END IF;
    UPDATE TRAVEL
    SET
        comment = comment_msg,
        evaluation = evaluation_client,
        dfinal = CURRENT_TIMESTAMP
    WHERE (scooter = scooter_id AND dfinal is null);

    RAISE NOTICE 'Trip % ended at %', scooter_id, CURRENT_TIMESTAMP;
END;
$$;

--CALL endTrip(2, NULL, NULL);

-- endregion