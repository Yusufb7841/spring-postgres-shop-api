package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.Mitarbeiter;
import com.dbw.spring_boot.model.MitarbeiterCreateRequest;
import com.dbw.spring_boot.repositories.MitarbeiterRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/mitarbeiter")
public class MitarbeiterController {

    private final MitarbeiterRepository repo;

    public MitarbeiterController(MitarbeiterRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(name = "id", required = false) Long id) {
        if (id == null) {
            List<Mitarbeiter> all = repo.findAll();
            return ResponseEntity.ok(all);
        }
        return repo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MitarbeiterCreateRequest req) {
        long newId = repo.create(req);
        Mitarbeiter created = repo.findById(newId).orElseThrow();
        return ResponseEntity
                .created(URI.create("/mitarbeiter?id=" + newId))
                .body(created);
    }


    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("id") long id) {
        int affected = repo.deleteById(id);
        if (affected == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}