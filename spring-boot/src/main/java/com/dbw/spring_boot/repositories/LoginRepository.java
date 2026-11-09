package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.KundeResponse;
import com.dbw.spring_boot.model.Mitarbeiter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LoginRepository {

    private final JdbcTemplate jdbc;

    public LoginRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Mitarbeiter> loginMitarbeiter(long personalNr, String passwort) {
        String sql = "SELECT personal_nr, passwort, email, vorname, nachname " +
                "FROM mitarbeiter " +
                "WHERE personal_nr = ? AND passwort = ?";

        List<Mitarbeiter> res = jdbc.query(sql, (rs, rowNum) -> new Mitarbeiter(
                rs.getLong("personal_nr"),
                null, // never send the password back
                rs.getString("email"),
                rs.getString("vorname"),
                rs.getString("nachname")
        ), personalNr, passwort);

        return res.stream().findFirst();
    }

    public Optional<KundeResponse> loginKunde(String email, String passwort) {
        String sql = "SELECT kunde_id, email, vorname, nachname, passwort " +
                "FROM kunde " +
                "WHERE email = ? AND passwort = ?";

        List<KundeResponse> res = jdbc.query(sql, (rs, rowNum) -> new KundeResponse(
                rs.getLong("kunde_id"),
                rs.getString("email"),
                rs.getString("vorname"),
                rs.getString("nachname"),
                null, // never send the password back
                List.of() // the login response doesn't include addresses
        ), email, passwort);

        return res.stream().findFirst();
    }
}
