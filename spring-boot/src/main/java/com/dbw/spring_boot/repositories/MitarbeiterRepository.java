package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.Mitarbeiter;
import com.dbw.spring_boot.model.MitarbeiterCreateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MitarbeiterRepository {

    private final JdbcTemplate jdbc;

    public MitarbeiterRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Mitarbeiter> ROW_MAPPER = (rs, rowNum) -> new Mitarbeiter(
            rs.getLong("personal_nr"),
            rs.getString("passwort"),
            rs.getString("email"),
            rs.getString("vorname"),
            rs.getString("nachname")
    );

    public List<Mitarbeiter> findAll() {
        return jdbc.query(
                "SELECT personal_nr, passwort, email, vorname, nachname FROM mitarbeiter ORDER BY personal_nr",
                ROW_MAPPER
        );
    }

    public Optional<Mitarbeiter> findById(long id) {
        List<Mitarbeiter> res = jdbc.query(
                "SELECT personal_nr, passwort, email, vorname, nachname FROM mitarbeiter WHERE personal_nr = ?",
                ROW_MAPPER,
                id
        );
        return res.stream().findFirst();
    }

    public long create(MitarbeiterCreateRequest req) {
        return jdbc.queryForObject(
                "INSERT INTO mitarbeiter (vorname, nachname, email, passwort) VALUES (?, ?, ?, ?) RETURNING personal_nr",
                Long.class,
                req.vorname(),
                req.nachname(),
                req.email(),
                req.passwort()
        );
    }

    public int deleteById(long id) {
        return jdbc.update("DELETE FROM mitarbeiter WHERE personal_nr = ?", id);
    }
}
