package com.dbw.spring_boot.model;

public record Mitarbeiter(
        Long personalNr,
        String passwort,
        String email,
        String vorname,
        String nachname
) {}
