package com.dbw.spring_boot.model;

import java.math.BigDecimal;

public record Produkt(
        String sku,
        String name,
        BigDecimal preis,
        int lagerbestand,
        long angelegtVon
) {}
