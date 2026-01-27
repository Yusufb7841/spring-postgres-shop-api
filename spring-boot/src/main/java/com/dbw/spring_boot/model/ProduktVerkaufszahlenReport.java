package com.dbw.spring_boot.model;

import java.math.BigDecimal;

public record ProduktVerkaufszahlenReport(
        String sku,
        String name,
        long gesamtVerkaufteMenge,
        BigDecimal umsatz,
        long anzahlBestellungen
) {}