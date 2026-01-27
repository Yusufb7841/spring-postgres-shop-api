package com.dbw.spring_boot.model;

public record MitarbeiterUebersichtReport(
        long personalNr,
        long anzahlVerwalteterBestellungen,
        long anzahlAngelegterProdukte
) {}