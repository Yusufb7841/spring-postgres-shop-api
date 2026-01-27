package com.dbw.spring_boot.model;

import java.math.BigDecimal;

public record KundeSummeAnzahlBestellungenReport(
        long kundeId,
        String email,
        int anzahlBestellungen,
        BigDecimal gesamtsumme
) {}