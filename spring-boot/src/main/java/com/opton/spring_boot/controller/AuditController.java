package com.opton.spring_boot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opton.spring_boot.audit.Audit;

@RestController
@RequestMapping("/audit")
public class AuditController {
    
    @GetMapping("/degree")
    public ResponseEntity<Audit> handleDegreeAudit() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping("/degree")
    public ResponseEntity<Audit> handleOptionAudit() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
