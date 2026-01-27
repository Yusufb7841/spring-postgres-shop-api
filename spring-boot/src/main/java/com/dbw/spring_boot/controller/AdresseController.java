package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.Adresse;
import com.dbw.spring_boot.model.AdresseCreateRequest;
import com.dbw.spring_boot.model.AdresseUpdateRequest;
import com.dbw.spring_boot.repositories.AdresseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/adressen")
public class AdresseController {

    private final AdresseRepository repo;

    public AdresseController(AdresseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(name = "id", required = false) Long id) {
        if (id == null) {
            List<Adresse> all = repo.findAll();
            return ResponseEntity.ok(all);
        }

        return repo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Adresse> create(@RequestBody AdresseCreateRequest req) {
        long newId = repo.create(req);
        Adresse created = repo.findById(newId).orElseThrow();
        return ResponseEntity.created(URI.create("/adressen?id=" + newId)).body(created);
    }

    @PutMapping
    public ResponseEntity<Adresse> update(@RequestParam("id") long id, @RequestBody AdresseUpdateRequest req) {
        int affected = repo.update(id, req);
        if (affected == 0) return ResponseEntity.notFound().build();
        Adresse updated = repo.findById(id).orElseThrow();
        return ResponseEntity.ok(updated);
    }
}
