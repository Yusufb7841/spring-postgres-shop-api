package com.dbw.spring_boot.model;

import java.math.BigDecimal;

public record ProduktMini(
        String sku,
        String name,
        BigDecimal preis,
        int lagerbestand,
        long angelegtVon
) {}
