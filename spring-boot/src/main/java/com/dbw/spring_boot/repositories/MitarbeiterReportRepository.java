package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.MitarbeiterUebersichtReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MitarbeiterReportRepository {

    private final JdbcTemplate jdbc;

    public MitarbeiterReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MitarbeiterUebersichtReport> findUebersicht() {
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
}