TRUNCATE TABLE adresse, kunde, mitarbeiter, bestellung, bestellposition, produkt, kunde_hat_adressen RESTART IDENTITY CASCADE;


INSERT INTO adresse (aktiv, strasse, hausnummer, plz, ort, land) VALUES
(true,  'Hauptstrasse',       '12',  10115, 'Berlin',    'Deutschland'),
(true,  'Bahnhofweg',         '7a',  80331, 'Muenchen',  'Deutschland'),
(true,  'Gartenallee',        '45',  50667, 'Koeln',     'Deutschland'),
(true,  'Schulweg',           '3',   20095, 'Hamburg',   'Deutschland'),
(true,  'Parkstrasse',        '88',  70173, 'Stuttgart', 'Deutschland'),
(true,  'Lindenweg',          '19b', 28195, 'Bremen',    'Deutschland'),
(true,  'Marktplatz',         '1',   01067, 'Dresden',   'Deutschland'),
(true,  'Kirchweg',           '22',  99084, 'Erfurt',    'Deutschland'),
(true,  'Feldstrasse',        '5',   34117, 'Kassel',    'Deutschland'),
(true,  'Seestrasse',         '100', 78462, 'Konstanz',  'Deutschland'),
(false,  'Universitätsstraße','1',   40225, 'Düsseldorf','Deutschland'),
(false,  'Heinrichweg',       '22a', 92083, 'Koeln',     'Deutschland'),
(false,  'Informatikweg',     '8b',  34217, 'Frankfurt', 'Deutschland'),
(false,  'Dbstrasse',         '42',  78312, 'Dbburg',    'Deutschland');


INSERT INTO kunde (email, vorname, nachname, passwort) VALUES
('123@test.db',     'Max',   'Muster',   'max12!'),
('1_1@firma.db',    'Anna',  'Schmidt',  'anna!1'),
('99-@shop.db',     'Peter', 'Mueller',  'peter9!9'),
('_42@uni.db',      'Laura', 'Meier',    'laura42!'),
('007@agentur.db',  'James', 'Bond',     'bond007!'),
('1-23@office.db',  'Sophie','Weber',    'sophie23!'),
('-5_5@cloud.db',   'Lukas', 'Fischer',  'lukas55!'),
('67@service.db',   'Maria', 'Keller',   'mari@67'),
('_78@portal.db',   'Jonas', 'Becker',   'jonas78!'),
('555@system.db',   'Nina',  'Hoffmann', 'nina!555');

INSERT INTO kunde_hat_adressen (adresse_id, kunde_id, typ) VALUES
(1, 1,  'Lieferadresse'),
(2, 1,  'Rechnungsadresse'),
(3, 2,  'Lieferadresse'),
(4, 2,  'Rechnungsadresse'),
(12,2,  'Rechnungsadresse'),
(5, 3,  'Lieferadresse'),
(6, 3,  'Rechnungsadresse'),
(13,3,  'Rechnungsadresse'),
(7, 4,  'Lieferadresse'),
(8, 4,  'Rechnungsadresse'),
(14,4,  'Lieferadresse'),
(9, 5,  'Lieferadresse'),
(10,5,  'Rechnungsadresse'),
(1, 6,  'Lieferadresse'),
(3, 6,  'Rechnungsadresse'),
(2, 7,  'Lieferadresse'),
(4, 7,  'Rechnungsadresse'),
(14,7,  'Rechnungsadresse'),
(5, 8,  'Lieferadresse'),
(6, 8,  'Rechnungsadresse'),
(7, 9,  'Lieferadresse'),
(8, 9,  'Rechnungsadresse'),
(11,9,  'Lieferadresse'),
(9, 10, 'Lieferadresse'),
(10,10, 'Rechnungsadresse');

INSERT INTO mitarbeiter (vorname, nachname, email, passwort) VALUES
('Anna', 'Meier',   'anna.meier@firma.db',   'a#123'),
('Zoe',  'Schmidt', 'zoe.schmidt@firma.db',  'Z!2bc'),
('Paul', 'Klein',   'paul.klein@firma.db',   '9$XYZ'),
('Maria','Fischer', 'maria.fischer@firma.db','m@789'),
('Quinn','Weber',   'quinn.weber@firma.db',  'Q5%foo'),
('Ben',  'Hartmann','ben.hartmann@firma.db', '1?Bar'),
('Xaver','Bauer',   'xaver.bauer@firma.db',  'x&777'),
('Peter','Wolf',    'peter.wolf@firma.db',   'P*abc9'),
('Tom',  'Neumann', 'tom.neumann@firma.db',  '7+Tes'),
('Lena', 'Koch',    'lena.koch@firma.db',    'A=999aaasdAASDASD');

INSERT INTO bestellung (kunde_id, mitarbeiterzuweis, datum, status) VALUES
(1,  1,  '2026-01-10 10:15:01+01', 'neu'),
(2,  2,  '2026-01-11 11:30:02+01', 'bezahlt'),
(3,  3,  '2025-12-12 14:45:03+01', 'versendet'),
(4,  4,  '2025-12-24 09:20:04+01', 'abgeschlossen'),
(5,  5,  '2025-12-25 16:00:05+01', 'neu'),
(6,  6,  '2025-12-26 13:10:06+01', 'bezahlt'),
(7,  7,  '2025-12-27 08:55:07+01', 'versendet'),
(8,  8,  '2025-12-29 17:40:08+01', 'storniert'),
(9,  9,  '2026-01-01 12:25:09+01', 'neu'),
(10, 10, '2026-01-02 15:05:10+01', 'abgeschlossen');

INSERT INTO produkt (sku, name, preis, lagerbestand, angelegt_von) VALUES
('SKU-1001', 'Notebook Basic',      699.99,  7,  1),
('SKU-1002', 'Notebook Pro',        1299.00, 8,  2),
('SKU-1003', 'Smartphone Mini',     399.50,  25, 3),
('SKU-1004', 'Smartphone Plus',     899.99,  4,  4),
('SKU-1005', 'Tablet 10 Zoll',      329.00,  20, 5),
('SKU-1006', 'Monitor 27 Zoll',     279.90,  3,  6),
('SKU-1007', 'Tastatur Mechanisch', 119.99,  100,7),
('SKU-1008', 'Maus Wireless',       49.95,   10, 8),
('SKU-1009', 'USB-C Dockingstation',189.00,  14, 9),
('SKU-1010', 'Headset Noise Cancel',79.99,   5,  10);

INSERT INTO bestellposition (sku, bestellung_id, menge) VALUES
('SKU-1001', 1,  5),
('SKU-1002', 1,  2),
('SKU-1003', 2,  1),
('SKU-1010', 2,  1),
('SKU-1004', 3,  1),
('SKU-1005', 3,  2),
('SKU-1007', 3,  4),
('SKU-1004', 4,  2),
('SKU-1005', 5,  3),
('SKU-1001', 5,  2),
('SKU-1002', 6,  1),
('SKU-1008', 6,  1),
('SKU-1006', 6,  1),
('SKU-1010', 6,  1),
('SKU-1008', 7,  2),
('SKU-1009', 8,  1),
('SKU-1006', 9,  2),
('SKU-1008', 9,  1),
('SKU-1010', 10, 1);