package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.KundeSummeAnzahlBestellungenReport;
import com.dbw.spring_boot.repositories.KundeSummeAnzahlBestellungenReportRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class KundeSummeAnzahlBestellungenReportController {

    private final KundeSummeAnzahlBestellungenReportRepository repo;

    public KundeSummeAnzahlBestellungenReportController(KundeSummeAnzahlBestellungenReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/kunde/summe-anzahl-bestellungen")
    public List<KundeSummeAnzahlBestellungenReport> kundeSummeAnzahlBestellungen() {
        return repo.kundeSummeAnzahlBestellungen();
    }
}