CREATE TABLE adresse (
                         adresse_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                         aktiv      BOOLEAN NOT NULL,
                         strasse    VARCHAR(60) NOT NULL CHECK (strasse ~ '^[A-ZÄÖÜ][a-zäöüß]{0,59}$'),
                         hausnummer VARCHAR(13) NOT NULL CHECK (hausnummer ~ '^[0-9]+[a-z]?$'),
                         plz        NUMERIC(12,0) NOT NULL CHECK (plz >= 0 AND plz <= 999999999999),
                         ort        VARCHAR(255) NOT NULL,
                         land       VARCHAR(255) NOT NULL
);

CREATE TABLE kunde (
                       kunde_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       email     VARCHAR(256) NOT NULL UNIQUE
                           CHECK (email LIKE '%@%.%' AND position('@' in email) < position('.' in email)),
                       vorname   VARCHAR(32) NOT NULL
                           CHECK (vorname ~ '^[A-Za-zÄÖÜäöüß]{1,32}$'),
                       nachname  VARCHAR(32) NOT NULL
                           CHECK (nachname ~ '^[A-Za-zÄÖÜäöüß]{1,32}$'),
                       passwort  VARCHAR(20) NOT NULL
                           CHECK (char_length(passwort) BETWEEN 5 AND 20)
                           CHECK (passwort ~ '[A-Za-zÄÖÜäöüß]'),
                           CHECK (passwort ~ '[0-9]'),
                           CHECK (passwort ~ '[^A-Za-zÄÖÜäöüß0-9]')
);

CREATE TABLE mitarbeiter (
                             personal_nr BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             vorname     VARCHAR(255) NOT NULL,
                             nachname    VARCHAR(255) NOT NULL,
                             email       VARCHAR(255) NOT NULL,
                             passwort    VARCHAR(20) NOT NULL
                                 CHECK (char_length(passwort) BETWEEN 5 AND 20)
                                 CHECK (passwort ~ '[A-Za-zÄÖÜäöüß]'),
                                 CHECK (passwort ~ '[0-9]'),
                                 CHECK (passwort ~ '[^A-Za-zÄÖÜäöüß0-9]')
);

CREATE TABLE produkt (
                         sku               VARCHAR(64) PRIMARY KEY CHECK (sku ~ '^[A-Za-z0-9-]+$'),
                         name              VARCHAR(255) NOT NULL,
                         preis             NUMERIC(10,2) NOT NULL CHECK (preis >= 0),
                         lagerbestand      INTEGER NOT NULL CHECK (lagerbestand >= 0),
                         angelegt_von      BIGINT NOT NULL REFERENCES mitarbeiter(personal_nr) ON DELETE CASCADE
);

CREATE TABLE bestellung (
                            bestellung_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            datum             TIMESTAMP NOT NULL,
                            status            VARCHAR(20) NOT NULL
                                CHECK (status IN ('neu', 'bezahlt', 'versendet', 'abgeschlossen', 'storniert')),
                            mitarbeiterzuweis BIGINT NOT NULL REFERENCES mitarbeiter(personal_nr) ON DELETE CASCADE,
                            kunde_id          BIGINT NOT NULL REFERENCES kunde(kunde_id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION trg_bestellung_status_guard()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status = 'storniert' AND OLD.status <> 'storniert' THEN
        IF OLD.status NOT IN ('neu', 'bezahlt') THEN
            RAISE EXCEPTION 'Status darf nur aus neu oder bezahlt auf storniert geändert werden (alt=%)', OLD.status;
END IF;
END IF;
RETURN NEW;
END;
$$;

CREATE TRIGGER bestellung_before_status_update
    BEFORE UPDATE OF status ON bestellung
    FOR EACH ROW
    EXECUTE FUNCTION trg_bestellung_status_guard();

CREATE TABLE kunde_hat_adressen (
                                    adresse_id BIGINT NOT NULL REFERENCES adresse(adresse_id) ON DELETE CASCADE,
                                    kunde_id   BIGINT NOT NULL REFERENCES kunde(kunde_id)   ON DELETE CASCADE,
                                    typ        VARCHAR(16) NOT NULL CHECK (typ IN ('Lieferadresse', 'Rechnungsadresse')),
                                    PRIMARY KEY (adresse_id, kunde_id, typ)
);

CREATE TABLE bestellposition (
                                 position_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 menge         INTEGER NOT NULL CHECK (menge > 0),
                                 sku           VARCHAR(64) NOT NULL REFERENCES produkt(sku) ON DELETE CASCADE,
                                 bestellung_id BIGINT NOT NULL REFERENCES bestellung(bestellung_id) ON DELETE CASCADE
);

CREATE OR REPLACE FUNCTION trg_bestellposition_ins()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
st TEXT;
    lb INTEGER;
BEGIN
SELECT status INTO st FROM bestellung WHERE bestellung_id = NEW.bestellung_id;
IF st IS NULL THEN
        RAISE EXCEPTION 'Bestellung % existiert nicht', NEW.bestellung_id;
END IF;

    IF st <> 'storniert' THEN
SELECT lagerbestand INTO lb
FROM produkt
WHERE sku = NEW.sku
    FOR UPDATE;

IF lb < NEW.menge THEN
            RAISE EXCEPTION 'Nicht genug Lagerbestand für % (verfügbar %, benötigt %)', NEW.sku, lb, NEW.menge;
END IF;

UPDATE produkt
SET lagerbestand = lagerbestand - NEW.menge
WHERE sku = NEW.sku;
END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER bestellposition_before_insert
    BEFORE INSERT ON bestellposition
    FOR EACH ROW
    EXECUTE FUNCTION trg_bestellposition_ins();

CREATE OR REPLACE FUNCTION trg_bestellposition_upd()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
st_old TEXT;
    st_new TEXT;
    lb INTEGER;
    diff INTEGER;
BEGIN
SELECT status INTO st_old FROM bestellung WHERE bestellung_id = OLD.bestellung_id;
SELECT status INTO st_new FROM bestellung WHERE bestellung_id = NEW.bestellung_id;

IF st_old IS NULL OR st_new IS NULL THEN
        RAISE EXCEPTION 'Bestellung existiert nicht (alt=% neu=%)', OLD.bestellung_id, NEW.bestellung_id;
END IF;

    IF st_old <> 'storniert' THEN
UPDATE produkt
SET lagerbestand = lagerbestand + OLD.menge
WHERE sku = OLD.sku;
END IF;

    IF st_new <> 'storniert' THEN
SELECT lagerbestand INTO lb
FROM produkt
WHERE sku = NEW.sku
    FOR UPDATE;

IF lb < NEW.menge THEN
            RAISE EXCEPTION 'Nicht genug Lagerbestand für % (verfügbar %, benötigt %)', NEW.sku, lb, NEW.menge;
END IF;

UPDATE produkt
SET lagerbestand = lagerbestand - NEW.menge
WHERE sku = NEW.sku;
END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER bestellposition_before_update
    BEFORE UPDATE ON bestellposition
    FOR EACH ROW
    EXECUTE FUNCTION trg_bestellposition_upd();

CREATE OR REPLACE FUNCTION trg_bestellposition_del()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
st TEXT;
BEGIN
SELECT status INTO st FROM bestellung WHERE bestellung_id = OLD.bestellung_id;
IF st IS NULL THEN
        RAISE EXCEPTION 'Bestellung % existiert nicht', OLD.bestellung_id;
END IF;

    IF st <> 'storniert' THEN
UPDATE produkt
SET lagerbestand = lagerbestand + OLD.menge
WHERE sku = OLD.sku;
END IF;

RETURN OLD;
END;
$$;

CREATE TRIGGER bestellposition_before_delete
    BEFORE DELETE ON bestellposition
    FOR EACH ROW
    EXECUTE FUNCTION trg_bestellposition_del();

CREATE OR REPLACE FUNCTION trg_bestellung_to_storniert()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status = 'storniert' AND OLD.status <> 'storniert' THEN
UPDATE produkt p
SET lagerbestand = p.lagerbestand + x.summe
    FROM (
            SELECT sku, SUM(menge) AS summe
            FROM bestellposition
            WHERE bestellung_id = NEW.bestellung_id
            GROUP BY sku
        ) x
WHERE p.sku = x.sku;
END IF;

RETURN NEW;
END;
$$;

CREATE TRIGGER bestellung_after_status_update
    AFTER UPDATE OF status ON bestellung
    FOR EACH ROW
    EXECUTE FUNCTION trg_bestellung_to_storniert();

CREATE OR REPLACE VIEW v_kunde_summe_anzahl_bestellungen AS
SELECT
    k.kunde_id AS kunde_id,
    k.email    AS email,
    COUNT(DISTINCT b.bestellung_id) AS anzahl_bestellungen,
    COALESCE(SUM(p.preis * bp.menge), 0) AS gesamtsumme
FROM kunde k
         LEFT JOIN bestellung b
                   ON b.kunde_id = k.kunde_id
         LEFT JOIN bestellposition bp
                   ON bp.bestellung_id = b.bestellung_id
         LEFT JOIN produkt p
                   ON p.sku = bp.sku
GROUP BY k.kunde_id, k.email;

CREATE OR REPLACE VIEW v_produkt_verkaufszahlen AS
SELECT
    p.sku,
    p.name,
    COALESCE(
            SUM(
                    CASE
                        WHEN b.status NOT IN ('neu', 'storniert') THEN bp.menge
                        ELSE 0
                        END
            ), 0
    ) AS gesamt_verkaufte_menge,
    COALESCE(
            SUM(
                    CASE
                        WHEN b.status NOT IN ('neu', 'storniert') THEN (bp.menge * p.preis)
                        ELSE 0
                        END
            ), 0
    ) AS umsatz,
    COALESCE(
            COUNT(
                    DISTINCT CASE
                                 WHEN b.status NOT IN ('neu', 'storniert') THEN b.bestellung_id
                                 ELSE NULL
                END
            ), 0
    ) AS anzahl_bestellungen
FROM produkt p
         LEFT JOIN bestellposition bp ON bp.sku = p.sku
         LEFT JOIN bestellung b ON b.bestellung_id = bp.bestellung_id
GROUP BY p.sku, p.name
ORDER BY gesamt_verkaufte_menge DESC, p.sku;

CREATE OR REPLACE VIEW v_mitarbeiter_uebersicht AS
SELECT
    m.personal_nr,
    COALESCE(COUNT(DISTINCT b.bestellung_id), 0) AS anzahl_verwalteter_bestellungen,
    COALESCE(COUNT(DISTINCT p.sku), 0)          AS anzahl_angelegter_produkte
FROM mitarbeiter m
         LEFT JOIN bestellung b ON b.mitarbeiterzuweis = m.personal_nr
         LEFT JOIN produkt p    ON p.angelegt_von      = m.personal_nr
GROUP BY m.personal_nr
ORDER BY m.personal_nr;

CREATE OR REPLACE VIEW v_mitarbeiter_bestellstatus_uebersicht AS
WITH status_liste AS (
    SELECT unnest(ARRAY['neu','bezahlt','versendet','abgeschlossen','storniert']) AS status
)
SELECT
    m.personal_nr,
    s.status,
    COALESCE(COUNT(b.bestellung_id), 0) AS anzahl_bestellungen
FROM mitarbeiter m
         CROSS JOIN status_liste s
         LEFT JOIN bestellung b
                   ON b.mitarbeiterzuweis = m.personal_nr
                       AND b.status = s.status
GROUP BY m.personal_nr, s.status
ORDER BY m.personal_nr, s.status;
