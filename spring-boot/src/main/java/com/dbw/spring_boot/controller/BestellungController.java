package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.BestellungCreateRequest;
import com.dbw.spring_boot.model.BestellungResponse;
import com.dbw.spring_boot.repositories.BestellungRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/bestellungen")
public class BestellungController {

    private final BestellungRepository repo;

    public BestellungController(BestellungRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam(name = "id", required = false) Long id) {
        if (id == null) {
            List<BestellungResponse> all = repo.findAll();
            return ResponseEntity.ok(all);
        }

        return repo.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody BestellungCreateRequest req) {
        long newId = repo.create(req);
        BestellungResponse created = repo.findById(newId).orElseThrow();
        return ResponseEntity.created(URI.create("/bestellungen?id=" + newId)).body(created);
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam("id") long id) {
        int affected = repo.deleteById(id);
        if (affected == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
