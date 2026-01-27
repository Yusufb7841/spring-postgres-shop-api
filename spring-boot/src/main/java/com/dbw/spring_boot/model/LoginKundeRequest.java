package com.dbw.spring_boot.model;

public record LoginKundeRequest(
        String email,
        String passwort
) {}
