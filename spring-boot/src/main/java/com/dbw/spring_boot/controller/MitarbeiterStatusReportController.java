package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.MitarbeiterBestellstatusReport;
import com.dbw.spring_boot.repositories.MitarbeiterStatusReportRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class MitarbeiterStatusReportController {

    private final MitarbeiterStatusReportRepository repo;

    public MitarbeiterStatusReportController(MitarbeiterStatusReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/mitarbeiter/bestellstatus-uebersicht")
    public List<MitarbeiterBestellstatusReport> bestellstatusUebersicht() {
        return repo.findAll();
    }
}