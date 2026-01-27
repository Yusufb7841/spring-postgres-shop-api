package com.dbw.spring_boot.model;

import java.util.List;

public record BestellungResponse(
        long bestellungId,
        long kundeId,
        long personalNr,
        String datum,
        String status,
        List<BestellungPositionResponse> positionen
) {}
