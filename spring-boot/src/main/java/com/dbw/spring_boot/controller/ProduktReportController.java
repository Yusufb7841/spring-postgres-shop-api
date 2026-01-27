package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.ProduktVerkaufszahlenReport;
import com.dbw.spring_boot.repositories.ProduktReportRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
public class ProduktReportController {

    private final ProduktReportRepository repo;

    public ProduktReportController(ProduktReportRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/produkt/verkaufszahlen")
    public List<ProduktVerkaufszahlenReport> produktVerkaufszahlen() {
        return repo.findVerkaufszahlen();
    }
}