package com.dbw.spring_boot;

import com.dbw.spring_boot.model.BestellpositionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The bestellposition triggers keep produkt.lagerbestand in sync with the order items. */
class StockTriggerTest extends IntegrationTest {

    @Test
    void addingAnOrderItemReducesStock() {
        assertThat(stock("SKU-1003")).isEqualTo(24);

        ResponseEntity<BestellpositionResponse> res = api.postForEntity("/bestellpositionen",
                Map.of("bestellungId", 1, "produktSku", "SKU-1003", "menge", 2), BestellpositionResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(stock("SKU-1003")).isEqualTo(22);
    }

    @Test
    void removingAnOrderItemPutsItsQuantityBackIntoStock() {
        BestellpositionResponse created = api.postForObject("/bestellpositionen",
                Map.of("bestellungId", 1, "produktSku", "SKU-1003", "menge", 2), BestellpositionResponse.class);
        assertThat(stock("SKU-1003")).isEqualTo(22);

        api.delete("/bestellpositionen?id=" + created.positionsId());

        assertThat(stock("SKU-1003")).isEqualTo(24);
    }

    @Test
    void deletingAnOrderPutsItsItemsBackIntoStock() {
        // order 1 is 'neu' and contains 5 x SKU-1001 and 2 x SKU-1002
        assertThat(stock("SKU-1001")).isEqualTo(0);
        assertThat(stock("SKU-1002")).isEqualTo(5);

        ResponseEntity<Void> res = api.exchange("/bestellungen?id=1", HttpMethod.DELETE, null, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(stock("SKU-1001")).isEqualTo(5);
        assertThat(stock("SKU-1002")).isEqualTo(7);
    }

    @Test
    void apiRejectsOrderingMoreThanIsInStock() {
        ResponseEntity<String> res = api.postForEntity("/bestellpositionen",
                Map.of("bestellungId", 1, "produktSku", "SKU-1003", "menge", 999), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(stock("SKU-1003")).isEqualTo(24);
    }

    @Test
    void databaseRejectsOversellingEvenWithoutTheApi() {
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO bestellposition (sku, bestellung_id, menge) VALUES ('SKU-1003', 1, 999)"))
                .isInstanceOf(DataAccessException.class)
                .rootCause().hasMessageContaining("Nicht genug Lagerbestand für SKU-1003");

        assertThat(stock("SKU-1003")).isEqualTo(24);
    }
}
