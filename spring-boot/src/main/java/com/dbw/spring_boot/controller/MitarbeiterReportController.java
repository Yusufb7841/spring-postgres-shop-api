package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.MitarbeiterUebersichtReport;
import com.dbw.spring_boot.repositories.MitarbeiterReportRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class MitarbeiterReportController {

    private final MitarbeiterReportRepository repo;

    public MitarbeiterReportController(MitarbeiterReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/mitarbeiter/uebersicht")
    public List<MitarbeiterUebersichtReport> mitarbeiterUebersicht() {
        return repo.findUebersicht();
    }
}