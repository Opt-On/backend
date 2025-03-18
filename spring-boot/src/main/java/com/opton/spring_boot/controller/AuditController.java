package com.opton.spring_boot.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.audit.Audit;
import com.opton.spring_boot.audit.AuditFactory;
import com.opton.spring_boot.plan.PlanCSVParser;
import com.opton.spring_boot.transcript_parser.TranscriptParser;
import com.opton.spring_boot.transcript_parser.types.Summary;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private Firestore firestore;

    @PostMapping("/declared")
    public ResponseEntity<Audit> handleDeclaredAudit(@RequestParam("transcript") MultipartFile file) {
        try {
            Summary summary = TranscriptParser.ParseTranscript(file);

            URL resource = getClass().getClassLoader().getResource("ME2023-test.csv");
            if (resource == null) {
                throw new FileNotFoundException("File not found in classpath");
            }
            File csvFile = new File(resource.toURI());
            FileReader fileReader = new FileReader(csvFile);

            PlanCSVParser parser = new PlanCSVParser();
            parser.csvIn(fileReader);

            Audit audit = AuditFactory.getAudit(parser.getPlans().get(0), summary, parser.getLists());

            return ResponseEntity.status(HttpStatus.OK).body(audit);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/whatif")
    public ResponseEntity<Audit> handleWhatifAudit(
            @RequestParam("email") String email,
            @RequestParam("plan") String plan) {
        try {
            DocumentSnapshot document = firestore.collection("user").document(email).get().get(); 

            if (!document.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Summary summary = document.toObject(Summary.class);

            URL resource = getClass().getClassLoader().getResource(plan);
            if (resource == null) {
                throw new FileNotFoundException("File not found in classpath");
            }
            File csvFile = new File(resource.toURI());
            FileReader fileReader = new FileReader(csvFile);

            PlanCSVParser parser = new PlanCSVParser();
            parser.csvIn(fileReader);

            Audit audit = AuditFactory.getAudit(parser.getPlans().get(0), summary, parser.getLists());

            return ResponseEntity.status(HttpStatus.OK).body(audit);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
