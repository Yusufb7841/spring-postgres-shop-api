package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.BestellpositionCreateRequest;
import com.dbw.spring_boot.model.BestellpositionResponse;
import com.dbw.spring_boot.repositories.BestellpositionRepository;
import com.dbw.spring_boot.repositories.BestellungRepository;
import com.dbw.spring_boot.repositories.ProduktRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bestellpositionen")
public class BestellpositionController {

    private final BestellpositionRepository repo;
    private final BestellungRepository bestellungRepo;
    private final ProduktRepository produktRepo;

    public BestellpositionController(BestellpositionRepository repo,
                                     BestellungRepository bestellungRepo,
                                     ProduktRepository produktRepo) {
        this.repo = repo;
        this.bestellungRepo = bestellungRepo;
        this.produktRepo = produktRepo;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(name = "id", required = false) Long id) {
        if (id == null) {
            List<BestellpositionResponse> all = repo.findAll();
            return ResponseEntity.ok(all);
        }

        return repo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BestellpositionResponse> create(@RequestBody BestellpositionCreateRequest req) {
        // basic validation -> 400
        if (req == null || req.bestellungId() == null || req.menge() == null
                || req.produktSku() == null || req.produktSku().isBlank()
                || req.menge() <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // unknown order -> 409
        if (bestellungRepo.findById(req.bestellungId()).isEmpty()) {
            return ResponseEntity.status(409).build();
        }

        // product must exist
        var prodOpt = produktRepo.findBySku(req.produktSku());
        if (prodOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // quantity must not exceed the stock -> 409 (the database trigger enforces this too)
        if (req.menge() > prodOpt.get().lagerbestand()) {
            return ResponseEntity.status(409).build();
        }

        long newId = repo.create(req);
        BestellpositionResponse created = repo.findById(newId).orElseThrow();
        return ResponseEntity.created(URI.create("/bestellpositionen?id=" + newId)).body(created);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("id") long id) {
        int affected = repo.deleteById(id);
        if (affected == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}