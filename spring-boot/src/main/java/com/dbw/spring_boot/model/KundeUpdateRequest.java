package com.dbw.spring_boot.model;

public record KundeUpdateRequest(
        String email,
        String vorname,
        String nachname,
        String passwort
) {}
