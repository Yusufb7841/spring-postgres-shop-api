package com.dbw.spring_boot.model;

public record Kunde(
        long kundeId,
        String email,
        String vorname,
        String nachname,
        String passwort
) {}
