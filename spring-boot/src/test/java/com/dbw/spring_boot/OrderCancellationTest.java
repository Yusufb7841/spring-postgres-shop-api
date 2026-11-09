package com.dbw.spring_boot;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The bestellung status triggers decide when an order may be cancelled and restock its items. */
class OrderCancellationTest extends IntegrationTest {

    @Test
    void shippedOrderCannotBeCancelled() {
        // order 3 has status 'versendet' in the seed data
        assertThatThrownBy(() -> jdbc.update("UPDATE bestellung SET status = 'storniert' WHERE bestellung_id = 3"))
                .isInstanceOf(DataAccessException.class)
                .rootCause().hasMessageContaining("Status darf nur aus neu oder bezahlt auf storniert geändert werden");

        assertThat(status(3)).isEqualTo("versendet");
    }

    @Test
    void cancellingANewOrderPutsAllItsItemsBackIntoStock() {
        // order 1 is 'neu' and contains 5 x SKU-1001 and 2 x SKU-1002
        assertThat(stock("SKU-1001")).isEqualTo(0);
        assertThat(stock("SKU-1002")).isEqualTo(5);

        jdbc.update("UPDATE bestellung SET status = 'storniert' WHERE bestellung_id = 1");

        assertThat(status(1)).isEqualTo("storniert");
        assertThat(stock("SKU-1001")).isEqualTo(5);
        assertThat(stock("SKU-1002")).isEqualTo(7);
    }

    private String status(long bestellungId) {
        return jdbc.queryForObject("SELECT status FROM bestellung WHERE bestellung_id = ?", String.class, bestellungId);
    }
}
