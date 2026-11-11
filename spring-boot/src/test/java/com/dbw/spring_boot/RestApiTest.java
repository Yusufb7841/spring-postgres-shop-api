package com.dbw.spring_boot;

import com.dbw.spring_boot.model.BestellungResponse;
import com.dbw.spring_boot.model.KundeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** HTTP behaviour of the REST endpoints: response shapes and status codes. */
class RestApiTest extends IntegrationTest {

    @Test
    void orderIsReturnedWithItsItemsAndLineTotals() {
        BestellungResponse order = api.getForObject("/bestellungen?id=1", BestellungResponse.class);

        assertThat(order.status()).isEqualTo("neu");
        assertThat(order.positionen()).hasSize(2);
        assertThat(order.positionen().get(0).produkt().sku()).isEqualTo("SKU-1001");
        assertThat(order.positionen().get(0).menge()).isEqualTo(5);
        assertThat(order.positionen().get(0).gesamtpreis()).isEqualByComparingTo(new BigDecimal("3499.95"));
    }

    @Test
    void creatingACustomerReturnsCreatedWithLocation() {
        ResponseEntity<KundeResponse> res = api.postForEntity("/kunden",
                Map.of("email", "lisa@test.db", "vorname", "Lisa", "nachname", "Lang", "passwort", "lisa1!"),
                KundeResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getHeaders().getLocation()).hasToString("/kunden?id=" + res.getBody().kundeId());
    }

    @Test
    void duplicateEmailReturnsConflict() {
        ResponseEntity<String> res = api.postForEntity("/kunden",
                Map.of("email", "123@test.db", "vorname", "Lisa", "nachname", "Lang", "passwort", "lisa1!"),
                String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void emailWithADotBeforeTheAtSignIsAccepted() {
        ResponseEntity<KundeResponse> res = api.postForEntity("/kunden",
                Map.of("email", "lisa.lang@test.db", "vorname", "Lisa", "nachname", "Lang", "passwort", "lisa1!"),
                KundeResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void dataViolatingACheckConstraintReturnsBadRequest() {
        ResponseEntity<String> res = api.postForEntity("/kunden",
                Map.of("email", "kaputt", "vorname", "Lisa", "nachname", "Lang", "passwort", "lisa1!"),
                String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unknownResourceReturnsNotFound() {
        assertThat(api.getForEntity("/kunden?id=999", String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(api.getForEntity("/produkte?sku=NOPE", String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void employeeWithOrdersCanBeDeleted() {
        // employee 1 handles order 1 and created SKU-1001, which is in orders 1 and 5
        ResponseEntity<Void> res = api.exchange("/mitarbeiter?id=1", HttpMethod.DELETE, null, Void.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void loginNeverReturnsThePassword() {
        ResponseEntity<KundeResponse> res = api.postForEntity("/login/kunde",
                Map.of("email", "123@test.db", "passwort", "max12!"), KundeResponse.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().email()).isEqualTo("123@test.db");
        assertThat(res.getBody().passwort()).isNull();
    }

    @Test
    void loginWithWrongPasswordReturnsUnauthorized() {
        ResponseEntity<String> res = api.postForEntity("/login/kunde",
                Map.of("email", "123@test.db", "passwort", "falsch"), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
