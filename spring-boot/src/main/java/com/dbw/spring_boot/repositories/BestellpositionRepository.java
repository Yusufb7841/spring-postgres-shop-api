package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.BestellpositionCreateRequest;
import com.dbw.spring_boot.model.BestellpositionResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class BestellpositionRepository {

    private final JdbcTemplate jdbc;

    public BestellpositionRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<BestellpositionResponse> ROW_MAPPER = (rs, rowNum) -> new BestellpositionResponse(
            rs.getLong("position_id"),
            rs.getLong("bestellung_id"),
            rs.getString("sku"),
            rs.getInt("menge"),
            rs.getBigDecimal("gesamtpreis")
    );

    public List<BestellpositionResponse> findAll() {
        String sql =
                "SELECT bp.position_id, bp.bestellung_id, bp.sku, bp.menge, " +
                        "       (bp.menge * p.preis) AS gesamtpreis " +
                        "FROM bestellposition bp " +
                        "JOIN produkt p ON p.sku = bp.sku " +
                        "ORDER BY bp.position_id";
        return jdbc.query(sql, ROW_MAPPER);
    }

    public Optional<BestellpositionResponse> findById(long id) {
        String sql =
                "SELECT bp.position_id, bp.bestellung_id, bp.sku, bp.menge, " +
                        "       (bp.menge * p.preis) AS gesamtpreis " +
                        "FROM bestellposition bp " +
                        "JOIN produkt p ON p.sku = bp.sku " +
                        "WHERE bp.position_id = ?";
        List<BestellpositionResponse> res = jdbc.query(sql, ROW_MAPPER, id);
        return res.stream().findFirst();
    }

    public long create(BestellpositionCreateRequest req) {
        return jdbc.queryForObject(
                "INSERT INTO bestellposition (bestellung_id, sku, menge) VALUES (?, ?, ?) RETURNING position_id",
                Long.class,
                req.bestellungId(),
                req.produktSku(),
                req.menge()
        );
    }

    public int deleteById(long id) {
        return jdbc.update("DELETE FROM bestellposition WHERE position_id = ?", id);
    }
}
