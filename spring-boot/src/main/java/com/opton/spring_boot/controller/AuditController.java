package com.opton.spring_boot.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/whatif")
    public ResponseEntity<Audit> handleWhatifAudit(@RequestHeader("email") String email,
            @RequestHeader("option") String option) {
        try {
            DocumentSnapshot document = firestore.collection("user").document(email).get().get();
            if (!document.exists())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Summary summary = document.toObject(Summary.class);

            Map<String, String> optionMap = new HashMap<String, String>() {
                {
                    put("COGSCOPT", "COGSCOPT2012.csv");
                    put("COMPENGOPT", "COMPENGOPT2024.csv");
                    put("MSCIOPT", "MSCIOPT2023.csv");
                    put("BIOMECHOPT", "BIOMECHOPT2023.csv");
                    put("BUSOPT", "BUSOPT2011.csv");
                    put("SWENGOPT", "SWENGOPT2024.csv");
                    put("ENTROPT", "ENTROPT2023.csv");
                    put("AIENGOPT", "AIENGOPT2023.csv");
                    put("COMPUOPT", "COMPUOPT2024.csv");
                    put("STATOPT", "STATOPT2023.csv");
                    put("MATHOPT", "MATHOPT2017.csv");
                    put("MECTROPT", "MECTROPT2023.csv");
                }
            };

            Audit audit = null;

            if (summary != null) {
                // degree
                Resource resource = new ClassPathResource("option/" + optionMap.get(option));
                if (!resource.exists()) {
                    throw new FileNotFoundException("file not found: option/" + optionMap.get(option));
                }

                try (FileReader fileReader = new FileReader(resource.getFile())) {
                    PlanCSVParser parser = new PlanCSVParser();
                    parser.csvIn(fileReader);

                    audit = AuditFactory.getAudit(parser.getPlans().get(0), summary, parser.getLists());
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(audit);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/options")
    public ResponseEntity<List<Map.Entry<String, double[]>>> handleAuditAllOptions(
            @RequestHeader("email") String email) {
        try {
            DocumentSnapshot document = firestore.collection("user").document(email).get().get();
            if (!document.exists())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Summary summary = document.toObject(Summary.class);

            URL resource = getClass().getClassLoader().getResource("option");
            if (resource == null)
                throw new FileNotFoundException("'option' folder not found");

            Map<Audit, double[]> auditMap = new HashMap<>();
            File[] files = new File(resource.toURI()).listFiles((dir, name) -> name.endsWith(".csv"));
            if (files != null) {
                for (File file : files) {
                    try (FileReader fileReader = new FileReader(file)) {
                        PlanCSVParser parser = new PlanCSVParser();
                        parser.csvIn(fileReader);
                        Audit audit = AuditFactory.getAudit(parser.getPlans().get(0), summary, parser.getLists());
                        double[] scores = audit.calculateProgress();
                        auditMap.put(audit, scores);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                }
            }

            Set<String> planNamesSeen = new HashSet<>();
            List<Map.Entry<String, double[]>> topAudits = auditMap.entrySet().stream()
                    .sorted((entry1, entry2) -> {
                        int val = Double.compare(entry2.getValue()[0] / entry2.getValue()[1],
                                entry1.getValue()[0] / entry1.getValue()[1]);
                        if (val == 0) {
                            return entry1.getKey().getPlan().getName().compareTo(entry2.getKey().getPlan().getName());
                        }
                        return val;
                    })
                    .filter(entry -> planNamesSeen.add(entry.getKey().getPlan().getName()))
                    .limit(3)
                    .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey().getPlan().getName(), entry.getValue()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(topAudits);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
