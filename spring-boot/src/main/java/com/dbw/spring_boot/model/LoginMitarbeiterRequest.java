package com.dbw.spring_boot.model;

public record LoginMitarbeiterRequest(
        long personalNr,
        String passwort
) {}
