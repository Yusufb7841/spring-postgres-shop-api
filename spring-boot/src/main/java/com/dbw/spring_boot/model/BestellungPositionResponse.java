package com.dbw.spring_boot.model;

import java.math.BigDecimal;

public record BestellungPositionResponse(
        long positionsId,
        long bestellungId,
        ProduktMini produkt,
        int menge,
        BigDecimal gesamtpreis
) {}
