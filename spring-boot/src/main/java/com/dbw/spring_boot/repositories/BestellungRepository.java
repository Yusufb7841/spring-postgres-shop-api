package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class BestellungRepository {

    private final JdbcTemplate jdbc;

    public BestellungRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String BASE_JOIN =
            "SELECT " +
                    "b.bestellung_id, b.kunde_id, b.mitarbeiterzuweis AS personal_nr, b.datum, b.status, " +
                    "bp.position_id, bp.menge, " +
                    "p.sku, p.name AS produkt_name, p.preis, p.lagerbestand, p.angelegt_von, " +
                    "(bp.menge * p.preis) AS gesamtpreis " +
                    "FROM bestellung b " +
                    "LEFT JOIN bestellposition bp ON bp.bestellung_id = b.bestellung_id " +
                    "LEFT JOIN produkt p ON p.sku = bp.sku ";

    private static final ResultSetExtractor<List<BestellungResponse>> EXTRACTOR = rs -> {
        Map<Long, BestellungAgg> map = new LinkedHashMap<>();

        while (rs.next()) {
            long bestellungId = rs.getLong("bestellung_id");

            BestellungAgg agg = map.get(bestellungId);
            if (agg == null) {
                agg = new BestellungAgg(
                        bestellungId,
                        rs.getLong("kunde_id"),
                        rs.getLong("personal_nr"),
                        rs.getString("datum"),
                        rs.getString("status")
                );
                map.put(bestellungId, agg);
            }

            Long posId = (Long) rs.getObject("position_id");
            if (posId != null) {
                Produkt produkt = new Produkt(
                        rs.getString("sku"),
                        rs.getString("produkt_name"),
                        rs.getBigDecimal("preis"),
                        rs.getInt("lagerbestand"),
                        rs.getLong("angelegt_von")
                );

                agg.positionen.add(new BestellungPositionResponse(
                        posId,
                        bestellungId,
                        produkt,
                        rs.getInt("menge"),
                        rs.getBigDecimal("gesamtpreis")
                ));
            }
        }

        List<BestellungResponse> out = new ArrayList<>(map.size());
        for (BestellungAgg a : map.values()) {
            out.add(new BestellungResponse(
                    a.bestellungId, a.kundeId, a.personalNr, a.datum, a.status, a.positionen
            ));
        }
        return out;
    };

    public List<BestellungResponse> findAll() {
        String sql = BASE_JOIN + "ORDER BY b.bestellung_id, bp.position_id";
        return jdbc.query(sql, EXTRACTOR);
    }

    public Optional<BestellungResponse> findById(long id) {
        String sql = BASE_JOIN + "WHERE b.bestellung_id = ? ORDER BY b.bestellung_id, bp.position_id";
        List<BestellungResponse> res = jdbc.query(sql, EXTRACTOR, id);
        assert res != null;
        return res.stream().findFirst();
    }

    public long create(BestellungCreateRequest req) {
        return jdbc.queryForObject(
                "INSERT INTO bestellung (kunde_id, mitarbeiterzuweis, datum, status) " +
                        "VALUES (?, ?, CAST(? AS timestamp), ?) RETURNING bestellung_id",
                Long.class,
                req.kundeId(),
                req.personalNr(),
                req.datum(),   // sent as text, cast to timestamp in the SQL above
                req.status()
        );
    }


    public int deleteById(long id) {
        return jdbc.update("DELETE FROM bestellung WHERE bestellung_id = ?", id);
    }

    private static final class BestellungAgg {
        final long bestellungId;
        final long kundeId;
        final long personalNr;
        final String datum;
        final String status;
        final List<BestellungPositionResponse> positionen = new ArrayList<>();

        private BestellungAgg(long bestellungId, long kundeId, long personalNr, String datum, String status) {
            this.bestellungId = bestellungId;
            this.kundeId = kundeId;
            this.personalNr = personalNr;
            this.datum = datum;
            this.status = status;
        }
    }
}
