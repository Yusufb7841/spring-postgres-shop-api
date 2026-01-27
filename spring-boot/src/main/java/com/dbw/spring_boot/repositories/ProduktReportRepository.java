package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.ProduktVerkaufszahlenReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProduktReportRepository {

    private final JdbcTemplate jdbc;

    public ProduktReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProduktVerkaufszahlenReport> findVerkaufszahlen() {
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