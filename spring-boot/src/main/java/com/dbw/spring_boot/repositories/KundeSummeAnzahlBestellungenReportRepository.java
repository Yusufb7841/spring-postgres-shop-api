package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.KundeSummeAnzahlBestellungenReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class KundeSummeAnzahlBestellungenReportRepository {

    private final JdbcTemplate jdbc;

    public KundeSummeAnzahlBestellungenReportRepository(JdbcTemplate jdbc) {
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
}