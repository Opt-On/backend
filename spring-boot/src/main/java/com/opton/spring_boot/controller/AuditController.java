package com.opton.spring_boot.controller;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opton.spring_boot.audit.Audit;
import com.opton.spring_boot.audit.AuditFactory;
import com.opton.spring_boot.audit.dto.*;
import com.opton.spring_boot.plan.PlanCSVParser;
import com.opton.spring_boot.transcript_parser.TranscriptParser;
import com.opton.spring_boot.transcript_parser.types.Summary;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private PlanCSVParser planCSVParser;
    
    @GetMapping("/degree")
    public ResponseEntity<Audit> handleDegreeAudit(@RequestParam("transcript") MultipartFile file) {
        try {
            Summary summary = TranscriptParser.ParseTranscript(file);

            FileReader fileReader = new FileReader("/ME2023.csv");
            planCSVParser.csvIn(fileReader);

            return ResponseEntity.status(HttpStatus.OK).body(AuditFactory.getAudit(planCSVParser.getPlans().get(0), summary, new ArrayList<Approval>()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/option")
    public ResponseEntity<Audit> handleOptionAudit() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
