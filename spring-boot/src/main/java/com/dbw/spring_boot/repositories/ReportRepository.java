package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.KundeSummeAnzahlBestellungenReport;
import com.dbw.spring_boot.model.MitarbeiterBestellstatusReport;
import com.dbw.spring_boot.model.MitarbeiterUebersichtReport;
import com.dbw.spring_boot.model.ProduktVerkaufszahlenReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportRepository {

    private final JdbcTemplate jdbc;

    public ReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<KundeSummeAnzahlBestellungenReport> kundeSummeAnzahlBestellungen() {
        String sql = """
            SELECT
                kunde_id,
                email,
                anzahl_bestellungen,
                gesamtsumme
            FROM v_kunde_summe_anzahl_bestellungen
            ORDER BY kunde_id ASC
            """;

        return jdbc.query(sql, (rs, rowNum) -> new KundeSummeAnzahlBestellungenReport(
                rs.getLong("kunde_id"),
                rs.getString("email"),
                rs.getInt("anzahl_bestellungen"),
                rs.getBigDecimal("gesamtsumme")
        ));
    }

    public List<MitarbeiterUebersichtReport> mitarbeiterUebersicht() {
        String sql = """
                SELECT personal_nr, anzahl_verwalteter_bestellungen, anzahl_angelegter_produkte
                FROM v_mitarbeiter_uebersicht
                """;

        return jdbc.query(sql, (rs, rowNum) -> new MitarbeiterUebersichtReport(
                rs.getLong("personal_nr"),
                rs.getLong("anzahl_verwalteter_bestellungen"),
                rs.getLong("anzahl_angelegter_produkte")
        ));
    }

    public List<MitarbeiterBestellstatusReport> mitarbeiterBestellstatusUebersicht() {
        String sql = """
                SELECT personal_nr, status, anzahl_bestellungen
                FROM v_mitarbeiter_bestellstatus_uebersicht
                """;

        return jdbc.query(sql, (rs, rowNum) -> new MitarbeiterBestellstatusReport(
                rs.getLong("personal_nr"),
                rs.getString("status"),
                rs.getLong("anzahl_bestellungen")
        ));
    }

    public List<ProduktVerkaufszahlenReport> produktVerkaufszahlen() {
        String sql = """
                SELECT sku, name, gesamt_verkaufte_menge, umsatz, anzahl_bestellungen
                FROM v_produkt_verkaufszahlen
                """;

        return jdbc.query(sql, (rs, rowNum) -> new ProduktVerkaufszahlenReport(
                rs.getString("sku"),
                rs.getString("name"),
                rs.getLong("gesamt_verkaufte_menge"),
                rs.getBigDecimal("umsatz"),
                rs.getLong("anzahl_bestellungen")
        ));
    }
}
