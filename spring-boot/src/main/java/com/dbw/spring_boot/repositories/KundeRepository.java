package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.Adresse;
import com.dbw.spring_boot.model.KundeAdresse;
import com.dbw.spring_boot.model.KundeCreateRequest;
import com.dbw.spring_boot.model.KundeResponse;
import com.dbw.spring_boot.model.KundeUpdateRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class KundeRepository {

    private final JdbcTemplate jdbc;

    public KundeRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_JOIN_QUERY =
            "SELECT " +
                    "k.kunde_id, k.email, k.vorname, k.nachname, k.passwort, " +
                    "a.adresse_id, a.aktiv, a.strasse, a.hausnummer, a.plz, a.ort, a.land, " +
                    "kha.typ " +
                    "FROM kunde k " +
                    "LEFT JOIN kunde_hat_adressen kha ON k.kunde_id = kha.kunde_id " +
                    "LEFT JOIN adresse a ON kha.adresse_id = a.adresse_id ";

    private static final ResultSetExtractor<List<KundeResponse>> KUNDEN_EXTRACTOR = rs -> {
        Map<Long, KundeAgg> map = new LinkedHashMap<>();

        while (rs.next()) {
            long kundeId = rs.getLong("kunde_id");

            KundeAgg agg = map.get(kundeId);
            if (agg == null) {
                agg = new KundeAgg(
                        kundeId,
                        rs.getString("email"),
                        rs.getString("vorname"),
                        rs.getString("nachname"),
                        rs.getString("passwort")
                );
                map.put(kundeId, agg);
            }

            Long adresseId = (Long) rs.getObject("adresse_id");
            if (adresseId != null) {
                Adresse adresse = new Adresse(
                        adresseId,
                        rs.getBoolean("aktiv"),
                        rs.getString("strasse"),
                        rs.getString("hausnummer"),
                        rs.getLong("plz"),
                        rs.getString("ort"),
                        rs.getString("land")
                );

                String typ = rs.getString("typ");
                agg.adressen.add(new KundeAdresse(adresse, typ));
            }
        }

        List<KundeResponse> out = new ArrayList<>(map.size());
        for (KundeAgg a : map.values()) {
            out.add(new KundeResponse(a.kundeId, a.email, a.vorname, a.nachname, a.passwort, a.adressen));
        }
        return out;
    };

    public List<KundeResponse> findAllWithAddresses() {
        String sql = BASE_JOIN_QUERY + "ORDER BY k.kunde_id, a.adresse_id";
        return jdbc.query(sql, KUNDEN_EXTRACTOR);
    }

    public Optional<KundeResponse> findByIdWithAddresses(long id) {
        String sql = BASE_JOIN_QUERY + "WHERE k.kunde_id = ? ORDER BY k.kunde_id, a.adresse_id";
        List<KundeResponse> res = jdbc.query(sql, KUNDEN_EXTRACTOR, id);
        assert res != null;
        return res.stream().findFirst();
    }

    public Optional<KundeResponse> findByEmailWithAddresses(String email) {
        String sql = BASE_JOIN_QUERY + "WHERE k.email = ? ORDER BY k.kunde_id, a.adresse_id";
        List<KundeResponse> res = jdbc.query(sql, KUNDEN_EXTRACTOR, email);
        assert res != null;
        return res.stream().findFirst();
    }

    public long create(KundeCreateRequest req) {
        return jdbc.queryForObject(
                "INSERT INTO kunde (email, vorname, nachname, passwort) VALUES (?, ?, ?, ?) RETURNING kunde_id",
                Long.class,
                req.email(),
                req.vorname(),
                req.nachname(),
                req.passwort()
        );
    }

    public int update(long id, KundeUpdateRequest req) {
        return jdbc.update(
                "UPDATE kunde SET email = ?, vorname = ?, nachname = ?, passwort = ? WHERE kunde_id = ?",
                req.email(),
                req.vorname(),
                req.nachname(),
                req.passwort(),
                id
        );
    }

    private static final class KundeAgg {
        final long kundeId;
        final String email;
        final String vorname;
        final String nachname;
        final String passwort;
        final List<KundeAdresse> adressen = new ArrayList<>();

        private KundeAgg(long kundeId, String email, String vorname, String nachname, String passwort) {
            this.kundeId = kundeId;
            this.email = email;
            this.vorname = vorname;
            this.nachname = nachname;
            this.passwort = passwort;
        }
    }
}
