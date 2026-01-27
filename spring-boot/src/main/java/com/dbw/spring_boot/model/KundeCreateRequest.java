package com.dbw.spring_boot.model;

public record KundeCreateRequest(
        String email,
        String vorname,
        String nachname,
        String passwort
) {}
