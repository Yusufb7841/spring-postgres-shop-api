package com.dbw.spring_boot.model;

import java.util.List;

public record KundeResponse(
        Long kundeId,
        String email,
        String vorname,
        String nachname,
        String passwort,
        List<KundeAdresse> adressen
) {}
