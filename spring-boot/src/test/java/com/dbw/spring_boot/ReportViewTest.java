package com.dbw.spring_boot;

import com.dbw.spring_boot.model.MitarbeiterBestellstatusReport;
import com.dbw.spring_boot.model.ProduktVerkaufszahlenReport;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/** The reporting views behind the /report endpoints. */
class ReportViewTest extends IntegrationTest {

    @Test
    void salesReportOnlyCountsOrdersThatAreNeitherNewNorCancelled() {
        Map<String, ProduktVerkaufszahlenReport> bySku = Arrays.stream(
                        api.getForObject("/report/produkt/verkaufszahlen", ProduktVerkaufszahlenReport[].class))
                .collect(Collectors.toMap(ProduktVerkaufszahlenReport::sku, Function.identity()));

        // SKU-1007: 4 units in order 3 ('versendet')
        assertThat(bySku.get("SKU-1007").gesamtVerkaufteMenge()).isEqualTo(4);
        assertThat(bySku.get("SKU-1007").umsatz()).isEqualByComparingTo(new BigDecimal("479.96"));
        // SKU-1001 only appears in orders 1 and 5, both still 'neu'
        assertThat(bySku.get("SKU-1001").gesamtVerkaufteMenge()).isZero();
    }

    @Test
    void statusReportListsEveryStatusForEveryEmployee() {
        MitarbeiterBestellstatusReport[] rows =
                api.getForObject("/report/mitarbeiter/bestellstatus-uebersicht", MitarbeiterBestellstatusReport[].class);

        // 10 employees x 5 statuses, including statuses with 0 orders
        assertThat(rows).hasSize(50);
        assertThat(Arrays.stream(rows).filter(r -> r.personalNr() == 1))
                .extracting(MitarbeiterBestellstatusReport::status, MitarbeiterBestellstatusReport::anzahlBestellungen)
                .containsExactlyInAnyOrder(
                        tuple("neu", 1L),
                        tuple("bezahlt", 0L),
                        tuple("versendet", 0L),
                        tuple("abgeschlossen", 0L),
                        tuple("storniert", 0L));
    }
}
