package com.dbw.spring_boot.model;

import java.math.BigDecimal;

public record BestellpositionResponse(
        long positionsId,
        long bestellungId,
        String produktSku,
        int menge,
        BigDecimal gesamtpreis
) {}
