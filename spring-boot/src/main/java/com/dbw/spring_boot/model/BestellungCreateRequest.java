package com.dbw.spring_boot.model;

public record BestellungCreateRequest(
        long kundeId,
        long personalNr,
        String datum,
        String status
) {}
