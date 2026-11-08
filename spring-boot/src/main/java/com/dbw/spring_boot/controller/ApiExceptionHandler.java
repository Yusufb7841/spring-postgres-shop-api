package com.dbw.spring_boot.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> invalid(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> dataIntegrity(DataIntegrityViolationException ex) {
        String sqlState = ex.getMostSpecificCause() instanceof SQLException sqlEx ? sqlEx.getSQLState() : "";
        // Postgres SQLState 23505 = unique violation
        if ("23505".equals(sqlState)) return ResponseEntity.status(409).build();
        // 23503 = foreign key violation, 23502 = not null violation, 23514 = check violation
        if ("23503".equals(sqlState) || "23502".equals(sqlState) || "23514".equals(sqlState)) {
            return ResponseEntity.badRequest().build();
        }
        // fallback
        return ResponseEntity.badRequest().build();
    }
}
