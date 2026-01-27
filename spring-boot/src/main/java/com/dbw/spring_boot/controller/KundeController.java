package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.KundeCreateRequest;
import com.dbw.spring_boot.model.KundeResponse;
import com.dbw.spring_boot.model.KundeUpdateRequest;
import com.dbw.spring_boot.repositories.KundeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/kunden")
public class KundeController {

    private final KundeRepository repo;

    public KundeController(KundeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<?> get(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "email", required = false) String email
    ) {
        if (id != null && email != null) {
            return ResponseEntity.badRequest().body("Bitte nur id ODER email angeben, nicht beides.");
        }

        if (id != null) {
            return repo.findByIdWithAddresses(id)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        if (email != null) {
            return repo.findByEmailWithAddresses(email)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }

        List<KundeResponse> all = repo.findAllWithAddresses();
        return ResponseEntity.ok(all);
    }

    @PostMapping
    public ResponseEntity<KundeResponse> create(@RequestBody KundeCreateRequest req) {
        long newId = repo.create(req);
        KundeResponse created = repo.findByIdWithAddresses(newId).orElseThrow();
        return ResponseEntity.created(URI.create("/kunden?id=" + newId)).body(created);
    }

    @PutMapping
    public ResponseEntity<KundeResponse> update(@RequestParam("id") long id, @RequestBody KundeUpdateRequest req) {
        int affected = repo.update(id, req);
        if (affected == 0) return ResponseEntity.notFound().build();
        KundeResponse updated = repo.findByIdWithAddresses(id).orElseThrow();
        return ResponseEntity.ok(updated);
    }
}
