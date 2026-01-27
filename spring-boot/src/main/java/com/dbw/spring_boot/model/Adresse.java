package com.dbw.spring_boot.model;

public record Adresse(
        long adresseId,
        boolean aktiv,
        String strasse,
        String hausnummer,
        long plz,
        String ort,
        String land
) {}
