package com.dbw.spring_boot.model;

public record MitarbeiterBestellstatusReport(
        long personalNr,
        String status,
        long anzahlBestellungen
) {}