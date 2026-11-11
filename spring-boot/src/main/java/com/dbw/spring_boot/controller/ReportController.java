package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.KundeSummeAnzahlBestellungenReport;
import com.dbw.spring_boot.model.MitarbeiterBestellstatusReport;
import com.dbw.spring_boot.model.MitarbeiterUebersichtReport;
import com.dbw.spring_boot.model.ProduktVerkaufszahlenReport;
import com.dbw.spring_boot.repositories.ReportRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class ReportController {

    private final ReportRepository repo;

    public ReportController(ReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/kunde/summe-anzahl-bestellungen")
    public List<KundeSummeAnzahlBestellungenReport> kundeSummeAnzahlBestellungen() {
        return repo.kundeSummeAnzahlBestellungen();
    }

    @GetMapping("/mitarbeiter/uebersicht")
    public List<MitarbeiterUebersichtReport> mitarbeiterUebersicht() {
        return repo.mitarbeiterUebersicht();
    }

    @GetMapping("/mitarbeiter/bestellstatus-uebersicht")
    public List<MitarbeiterBestellstatusReport> bestellstatusUebersicht() {
        return repo.mitarbeiterBestellstatusUebersicht();
    }

    @GetMapping("/produkt/verkaufszahlen")
    public List<ProduktVerkaufszahlenReport> produktVerkaufszahlen() {
        return repo.produktVerkaufszahlen();
    }
}
