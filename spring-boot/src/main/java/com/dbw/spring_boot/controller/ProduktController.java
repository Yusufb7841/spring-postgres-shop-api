package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.Produkt;
import com.dbw.spring_boot.model.ProduktCreateRequest;
import com.dbw.spring_boot.model.ProduktUpdateLagerbestandRequest;
import com.dbw.spring_boot.repositories.ProduktRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/produkte")
public class ProduktController {

    private final ProduktRepository repo;

    public ProduktController(ProduktRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(name = "sku", required = false) String sku) {
        if (sku == null) {
            List<Produkt> all = repo.findAll();
            return ResponseEntity.ok(all);
        }

        return repo.findBySku(sku)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProduktCreateRequest req) {
        int affected = repo.create(req);
        if (affected == 0) return ResponseEntity.badRequest().build();

        Produkt created = repo.findBySku(req.sku()).orElseThrow();
        return ResponseEntity
                .created(URI.create("/produkte?sku=" + req.sku()))
                .body(created);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("sku") String sku) {
        int affected = repo.deleteBySku(sku);
        if (affected == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping
    public ResponseEntity<?> patchLagerbestand(
            @RequestParam("sku") String sku,
            @RequestBody ProduktUpdateLagerbestandRequest req
    ) {
        if (req == null || req.lagerbestand() < 0) {
            return ResponseEntity.badRequest().build();
        }

        int affected = repo.updateLagerbestand(sku, req);
        if (affected == 0) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(repo.findBySku(sku).orElseThrow());
    }

}