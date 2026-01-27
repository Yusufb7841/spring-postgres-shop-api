package com.dbw.spring_boot.controller;

import com.dbw.spring_boot.model.*;
import com.dbw.spring_boot.repositories.LoginRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final LoginRepository repo;

    public LoginController(LoginRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/mitarbeiter")
    public ResponseEntity<Mitarbeiter> loginMitarbeiter(@RequestBody LoginMitarbeiterRequest req) {
        return repo.loginMitarbeiter(req.personalNr(), req.passwort())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).body(new Mitarbeiter(
                        null, null, null, null, null
                )));
    }

    @PostMapping("/kunde")
    public ResponseEntity<KundeResponse> loginKunde(@RequestBody LoginKundeRequest req) {
        return repo.loginKunde(req.email(), req.passwort())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).body(new KundeResponse(
                        null, null, null, null, null, List.of()
                )));
    }
}
