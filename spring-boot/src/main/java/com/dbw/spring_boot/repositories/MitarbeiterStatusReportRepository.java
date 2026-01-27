package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.MitarbeiterBestellstatusReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MitarbeiterStatusReportRepository {

    private final JdbcTemplate jdbc;

    public MitarbeiterStatusReportRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MitarbeiterBestellstatusReport> findAll() {
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
}