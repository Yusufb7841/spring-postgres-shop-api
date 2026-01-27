package com.dbw.spring_boot.model;

public record AdresseUpdateRequest(
        boolean aktiv,
        String strasse,
        String hausnummer,
        long plz,
        String ort,
        String land
) {}
