-- =============================================================================
--  Online shop database (PostgreSQL 17)
-- =============================================================================
--
--  1. Tables     customers, addresses, employees, products, orders, order items,
--                plus indexes on their foreign keys
--  2. Triggers   keep the stock and the order status consistent
--  3. Views      the reports behind the GET /report/... endpoints
--
--  The business rules live in the database itself: CHECK constraints validate
--  the data, and triggers keep the stock in sync with the order items. So the
--  rules apply to every client, not only to the REST API.
--
--  Docker runs this file once when the database is created, then data.sql
--  inserts the demo data.
-- =============================================================================


-- =============================================================================
--  1. Tables
-- =============================================================================

-- Addresses. Customers are linked to them through kunde_hat_adressen.
CREATE TABLE adresse (
    adresse_id  BIGINT         GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aktiv       BOOLEAN        NOT NULL,
    -- one word: a capital letter, then lowercase letters (e.g. 'Hauptstrasse')
    strasse     VARCHAR(60)    NOT NULL CHECK (strasse ~ '^[A-ZÄÖÜ][a-zäöüß]{0,59}$'),
    -- digits, optionally followed by one lowercase letter (e.g. '12', '7a')
    hausnummer  VARCHAR(13)    NOT NULL CHECK (hausnummer ~ '^[0-9]+[a-z]?$'),
    plz         NUMERIC(12,0)  NOT NULL CHECK (plz >= 0 AND plz <= 999999999999),
    ort         VARCHAR(255)   NOT NULL,
    land        VARCHAR(255)   NOT NULL
);

-- Customers. They log in with their email and password.
CREATE TABLE kunde (
    kunde_id  BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    -- an '@' followed later by a '.'
    email     VARCHAR(256)  NOT NULL UNIQUE CHECK (email LIKE '%@%.%'),
    -- letters only, including German umlauts
    vorname   VARCHAR(32)   NOT NULL CHECK (vorname  ~ '^[A-Za-zÄÖÜäöüß]{1,32}$'),
    nachname  VARCHAR(32)   NOT NULL CHECK (nachname ~ '^[A-Za-zÄÖÜäöüß]{1,32}$'),
    -- 5 to 20 characters with at least one letter, one digit and one special character
    passwort  VARCHAR(20)   NOT NULL
        CHECK (char_length(passwort) BETWEEN 5 AND 20)
        CHECK (passwort ~ '[A-Za-zÄÖÜäöüß]')
        CHECK (passwort ~ '[0-9]')
        CHECK (passwort ~ '[^A-Za-zÄÖÜäöüß0-9]')
);

-- Employees. They log in with their employee number (personal_nr),
-- create products and handle orders.
CREATE TABLE mitarbeiter (
    personal_nr  BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vorname      VARCHAR(255)  NOT NULL,
    nachname     VARCHAR(255)  NOT NULL,
    email        VARCHAR(255)  NOT NULL UNIQUE,
    -- same password rules as for customers
    passwort     VARCHAR(20)   NOT NULL
        CHECK (char_length(passwort) BETWEEN 5 AND 20)
        CHECK (passwort ~ '[A-Za-zÄÖÜäöüß]')
        CHECK (passwort ~ '[0-9]')
        CHECK (passwort ~ '[^A-Za-zÄÖÜäöüß0-9]')
);

-- Products, identified by their SKU (stock keeping unit).
-- lagerbestand is the current stock. The triggers in section 2 keep it up to date.
CREATE TABLE produkt (
    sku           VARCHAR(64)    PRIMARY KEY CHECK (sku ~ '^[A-Za-z0-9-]+$'),
    name          VARCHAR(255)   NOT NULL,
    preis         NUMERIC(10,2)  NOT NULL CHECK (preis >= 0),
    lagerbestand  INTEGER        NOT NULL CHECK (lagerbestand >= 0),
    -- the employee who created the product
    angelegt_von  BIGINT         NOT NULL REFERENCES mitarbeiter(personal_nr) ON DELETE CASCADE
);

-- Orders. Each order belongs to a customer and is handled by an employee.
-- Status flow: neu (new) -> bezahlt (paid) -> versendet (shipped) -> abgeschlossen (completed).
-- Orders that are still 'neu' or 'bezahlt' can be cancelled (storniert), see section 2.
CREATE TABLE bestellung (
    bestellung_id      BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    datum              TIMESTAMP    NOT NULL,
    status             VARCHAR(20)  NOT NULL
        CHECK (status IN ('neu', 'bezahlt', 'versendet', 'abgeschlossen', 'storniert')),
    -- the employee handling the order
    mitarbeiterzuweis  BIGINT       NOT NULL REFERENCES mitarbeiter(personal_nr) ON DELETE CASCADE,
    kunde_id           BIGINT       NOT NULL REFERENCES kunde(kunde_id) ON DELETE CASCADE
);

-- Links customers to addresses (n:m). The same address can be a customer's
-- delivery address (Lieferadresse), billing address (Rechnungsadresse) or both.
CREATE TABLE kunde_hat_adressen (
    adresse_id  BIGINT       NOT NULL REFERENCES adresse(adresse_id) ON DELETE CASCADE,
    kunde_id    BIGINT       NOT NULL REFERENCES kunde(kunde_id)     ON DELETE CASCADE,
    typ         VARCHAR(16)  NOT NULL CHECK (typ IN ('Lieferadresse', 'Rechnungsadresse')),
    PRIMARY KEY (adresse_id, kunde_id, typ)
);

-- Order items: how many units (menge) of which product an order contains.
CREATE TABLE bestellposition (
    position_id    BIGINT       GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    menge          INTEGER      NOT NULL CHECK (menge > 0),
    sku            VARCHAR(64)  NOT NULL REFERENCES produkt(sku) ON DELETE CASCADE,
    bestellung_id  BIGINT       NOT NULL REFERENCES bestellung(bestellung_id) ON DELETE CASCADE
);

-- PostgreSQL creates indexes for primary keys and UNIQUE columns automatically,
-- but not for foreign keys. These speed up the joins in the views and triggers,
-- and the lookups ON DELETE CASCADE does when a referenced row is deleted.
-- (kunde_hat_adressen.adresse_id is already covered by its primary key.)
CREATE INDEX idx_produkt_angelegt_von           ON produkt (angelegt_von);
CREATE INDEX idx_bestellung_mitarbeiterzuweis   ON bestellung (mitarbeiterzuweis);
CREATE INDEX idx_bestellung_kunde_id            ON bestellung (kunde_id);
CREATE INDEX idx_kunde_hat_adressen_kunde_id    ON kunde_hat_adressen (kunde_id);
CREATE INDEX idx_bestellposition_sku            ON bestellposition (sku);
CREATE INDEX idx_bestellposition_bestellung_id  ON bestellposition (bestellung_id);


-- =============================================================================
--  2. Triggers
-- =============================================================================
--
--  Stock: every order item takes its quantity out of produkt.lagerbestand.
--  Items in cancelled orders are skipped, because cancelling has already
--  put their quantity back.
--
--    item added      ->  stock goes down (rejected if there isn't enough)
--    item changed    ->  the old quantity goes back, the new one comes out
--    item removed    ->  the quantity goes back
--    order cancelled ->  the quantities of all its items go back
--
--  Status: an order can only be cancelled while it is 'neu' or 'bezahlt',
--  and a cancelled order stays cancelled.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- New order item: take its quantity out of stock.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_bestellposition_ins()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    bestellstatus  TEXT;     -- status of the order
    bestand        INTEGER;  -- current stock of the product
BEGIN
    SELECT status INTO bestellstatus FROM bestellung WHERE bestellung_id = NEW.bestellung_id;
    IF bestellstatus IS NULL THEN
        RAISE EXCEPTION 'Bestellung % existiert nicht', NEW.bestellung_id;
    END IF;

    IF bestellstatus <> 'storniert' THEN
        -- FOR UPDATE locks the product row until the transaction ends,
        -- so two orders at the same time can't both take the last units
        SELECT lagerbestand INTO bestand
        FROM produkt
        WHERE sku = NEW.sku
        FOR UPDATE;

        IF bestand < NEW.menge THEN
            RAISE EXCEPTION 'Nicht genug Lagerbestand für % (verfügbar %, benötigt %)', NEW.sku, bestand, NEW.menge;
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

-- ---------------------------------------------------------------------------
-- Changed order item: put the old quantity back, then take the new one.
-- This also works when the item is moved to another product or order.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_bestellposition_upd()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    status_alt  TEXT;     -- status of the order the item was in
    status_neu  TEXT;     -- status of the order the item is in now
    bestand     INTEGER;  -- current stock of the (new) product
BEGIN
    SELECT status INTO status_alt FROM bestellung WHERE bestellung_id = OLD.bestellung_id;
    SELECT status INTO status_neu FROM bestellung WHERE bestellung_id = NEW.bestellung_id;

    IF status_alt IS NULL OR status_neu IS NULL THEN
        RAISE EXCEPTION 'Bestellung existiert nicht (alt=% neu=%)', OLD.bestellung_id, NEW.bestellung_id;
    END IF;

    -- put the old quantity back
    IF status_alt <> 'storniert' THEN
        UPDATE produkt
        SET lagerbestand = lagerbestand + OLD.menge
        WHERE sku = OLD.sku;
    END IF;

    -- take the new quantity, with the same stock check as for a new item
    IF status_neu <> 'storniert' THEN
        SELECT lagerbestand INTO bestand
        FROM produkt
        WHERE sku = NEW.sku
        FOR UPDATE;

        IF bestand < NEW.menge THEN
            RAISE EXCEPTION 'Nicht genug Lagerbestand für % (verfügbar %, benötigt %)', NEW.sku, bestand, NEW.menge;
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

-- ---------------------------------------------------------------------------
-- Removed order item: put its quantity back into stock.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_bestellposition_del()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    bestellstatus  TEXT;  -- status of the order
BEGIN
    SELECT status INTO bestellstatus FROM bestellung WHERE bestellung_id = OLD.bestellung_id;
    IF bestellstatus IS NULL THEN
        RAISE EXCEPTION 'Bestellung % existiert nicht', OLD.bestellung_id;
    END IF;

    IF bestellstatus <> 'storniert' THEN
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

-- ---------------------------------------------------------------------------
-- Status change: check that it is allowed.
--   - A shipped or completed order can't be cancelled anymore.
--   - A cancelled order can't be reopened, because its stock has already been
--     put back and would otherwise be counted twice.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_bestellung_status_guard()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.status = 'storniert' AND OLD.status IN ('versendet', 'abgeschlossen') THEN
        RAISE EXCEPTION 'Status darf nur aus neu oder bezahlt auf storniert geändert werden (alt=%)', OLD.status;
    END IF;

    IF OLD.status = 'storniert' AND NEW.status <> 'storniert' THEN
        RAISE EXCEPTION 'Stornierte Bestellung % kann nicht wieder geöffnet werden', OLD.bestellung_id;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER bestellung_before_status_update
    BEFORE UPDATE OF status ON bestellung
    FOR EACH ROW
    EXECUTE FUNCTION trg_bestellung_status_guard();

-- ---------------------------------------------------------------------------
-- Cancelled order: put the quantities of all its items back into stock.
-- One UPDATE for the whole order; the subquery adds up the quantity per product.
-- Runs AFTER the update, so it only happens once the guard above has passed.
-- ---------------------------------------------------------------------------
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

-- ---------------------------------------------------------------------------
-- Deleted order: delete its items first, while the order still exists.
-- ON DELETE CASCADE would remove the order before its items, and the item
-- trigger above could then no longer read the order's status.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_bestellung_del()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    DELETE FROM bestellposition WHERE bestellung_id = OLD.bestellung_id;
    RETURN OLD;
END;
$$;

CREATE TRIGGER bestellung_before_delete
    BEFORE DELETE ON bestellung
    FOR EACH ROW
    EXECUTE FUNCTION trg_bestellung_del();


-- =============================================================================
--  3. Views (reports)
-- =============================================================================
--  Each view backs one GET /report/... endpoint. The LEFT JOINs keep rows
--  without any orders, so they show up with 0 instead of being left out.
-- =============================================================================

-- Number of orders and total order value per customer.
-- GET /report/kunde/summe-anzahl-bestellungen
CREATE OR REPLACE VIEW v_kunde_summe_anzahl_bestellungen AS
SELECT
    k.kunde_id,
    k.email,
    COUNT(DISTINCT b.bestellung_id)       AS anzahl_bestellungen,
    COALESCE(SUM(p.preis * bp.menge), 0)  AS gesamtsumme
FROM kunde k
LEFT JOIN bestellung      b  ON b.kunde_id       = k.kunde_id
LEFT JOIN bestellposition bp ON bp.bestellung_id = b.bestellung_id
LEFT JOIN produkt         p  ON p.sku            = bp.sku
GROUP BY k.kunde_id, k.email;

-- Units sold, revenue and number of orders per product, best sellers first.
-- An order only counts as a sale once it is past 'neu' and not cancelled.
-- GET /report/produkt/verkaufszahlen
CREATE OR REPLACE VIEW v_produkt_verkaufszahlen AS
SELECT
    p.sku,
    p.name,
    COALESCE(SUM(bp.menge)           FILTER (WHERE b.status NOT IN ('neu', 'storniert')), 0)
        AS gesamt_verkaufte_menge,
    COALESCE(SUM(bp.menge * p.preis) FILTER (WHERE b.status NOT IN ('neu', 'storniert')), 0)
        AS umsatz,
    COUNT(DISTINCT b.bestellung_id)  FILTER (WHERE b.status NOT IN ('neu', 'storniert'))
        AS anzahl_bestellungen
FROM produkt p
LEFT JOIN bestellposition bp ON bp.sku          = p.sku
LEFT JOIN bestellung      b  ON b.bestellung_id = bp.bestellung_id
GROUP BY p.sku, p.name
ORDER BY gesamt_verkaufte_menge DESC, p.sku;

-- Per employee: how many orders they handle and how many products they created.
-- Joining both tables multiplies the rows, so COUNT(DISTINCT ...) counts each one once.
-- GET /report/mitarbeiter/uebersicht
CREATE OR REPLACE VIEW v_mitarbeiter_uebersicht AS
SELECT
    m.personal_nr,
    COUNT(DISTINCT b.bestellung_id)  AS anzahl_verwalteter_bestellungen,
    COUNT(DISTINCT p.sku)            AS anzahl_angelegter_produkte
FROM mitarbeiter m
LEFT JOIN bestellung b ON b.mitarbeiterzuweis = m.personal_nr
LEFT JOIN produkt    p ON p.angelegt_von      = m.personal_nr
GROUP BY m.personal_nr
ORDER BY m.personal_nr;

-- Number of orders per employee and status, including statuses with 0 orders:
-- the CROSS JOIN first builds one row for every employee/status pair.
-- GET /report/mitarbeiter/bestellstatus-uebersicht
CREATE OR REPLACE VIEW v_mitarbeiter_bestellstatus_uebersicht AS
WITH status_liste AS (
    SELECT unnest(ARRAY['neu', 'bezahlt', 'versendet', 'abgeschlossen', 'storniert']) AS status
)
SELECT
    m.personal_nr,
    s.status,
    COUNT(b.bestellung_id) AS anzahl_bestellungen
FROM mitarbeiter m
CROSS JOIN status_liste s
LEFT JOIN bestellung b ON b.mitarbeiterzuweis = m.personal_nr
                      AND b.status = s.status
GROUP BY m.personal_nr, s.status
ORDER BY m.personal_nr, s.status;
