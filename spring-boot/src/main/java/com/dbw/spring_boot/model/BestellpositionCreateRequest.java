package com.dbw.spring_boot.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record BestellpositionCreateRequest(
        @JsonProperty("bestellungId") Long bestellungId,
        @JsonAlias({"produktSku", "sku"}) String produktSku,
        Integer menge
) {}