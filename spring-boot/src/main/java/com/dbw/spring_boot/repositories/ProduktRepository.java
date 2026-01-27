package com.dbw.spring_boot.repositories;

import com.dbw.spring_boot.model.Produkt;
import com.dbw.spring_boot.model.ProduktCreateRequest;
import com.dbw.spring_boot.model.ProduktUpdateLagerbestandRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProduktRepository {

    private final JdbcTemplate jdbc;

    public ProduktRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Produkt> ROW_MAPPER = (rs, rowNum) -> new Produkt(
            rs.getString("sku"),
            rs.getString("name"),
            rs.getBigDecimal("preis"),
            rs.getInt("lagerbestand"),
            rs.getLong("angelegt_von")
    );

    public List<Produkt> findAll() {
        return jdbc.query(
                "SELECT sku, name, preis, lagerbestand, angelegt_von FROM produkt ORDER BY sku",
                ROW_MAPPER
        );
    }

    public Optional<Produkt> findBySku(String sku) {
        List<Produkt> res = jdbc.query(
                "SELECT sku, name, preis, lagerbestand, angelegt_von FROM produkt WHERE sku = ?",
                ROW_MAPPER,
                sku
        );
        return res.stream().findFirst();
    }

    public int create(ProduktCreateRequest req) {
        return jdbc.update(
                "INSERT INTO produkt (sku, name, preis, lagerbestand, angelegt_von) VALUES (?, ?, ?, ?, ?)",
                req.sku(),
                req.name(),
                req.preis(),
                req.lagerbestand(),
                req.angelegtVon()
        );
    }

    public int deleteBySku(String sku) {
        return jdbc.update("DELETE FROM produkt WHERE sku = ?", sku);
    }

    public int updateLagerbestand(String sku, ProduktUpdateLagerbestandRequest req) {
        return jdbc.update(
                "UPDATE produkt SET lagerbestand = ? WHERE sku = ?",
                req.lagerbestand(),
                sku
        );
    }
}
