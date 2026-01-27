package com.dbw.spring_boot.model;

public record MitarbeiterCreateRequest(
        String passwort,
        String email,
        String vorname,
        String nachname
) {}
