package com.opton.spring_boot.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.audit.Audit;
import com.opton.spring_boot.audit.AuditFactory;
import com.opton.spring_boot.plan.PlanCSVParser;
import com.opton.spring_boot.transcript_parser.types.Summary;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private Firestore firestore;

    @PostMapping("/declared")
    public ResponseEntity<List<Audit>> handleDeclaredAudit(@RequestHeader("email") String email) {
        try {
            DocumentSnapshot document = firestore.collection("user").document(email).get().get();
            if (!document.exists())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Summary summary = document.toObject(Summary.class);

            Map<String, String> degreeMap = new HashMap<String, String>() {
                {
                    put("Computer Science", "AE2018.csv"); // FAKE 
                    put("Aerospace Engineering", "AE2018.csv");
                    put("Chemical Engineering", "CHE2023.csv");
                    put("Computer Engineering", "COMPE2023.csv");
                    put("Electrical Engineering", "ELE2023.csv");
                    put("Mechanical Engineering", "ME2023.csv");
                    put("Mechatronics Engineering", "MECTR2023.csv");
                    put("Management Engineering", "MGTE2023.csv");
                    put("Nanotechnology Engineering", "NE2018.csv");
                    put("Software Engineering", "SE2023.csv");
                    put("Architectural Engineering", "ARCHPPENG2017.csv");
                    put("Civil Engineering", "CIVE2017.csv");
                    put("Environmental Engineering", "ENVE2017.csv");
                    put("Geological Engineering", "GEOE2017.csv");
                    put("Systems Design Engineering", "SYDE2017.csv");
                }
            };

            Map<String, String> optionMap = new HashMap<String, String>() {
                {
                    put("Digital Hardware Option", "COGSCOPT2012.csv"); // FAKE
                    put("Cognitive Science Option", "COGSCOPT2012.csv");
                    put("Computer Engineering Option", "COMPENGOPT2024.csv");
                    put("Management Science Option", "MSCIOPT2023.csv");
                    put("Biomechanics Option", "BIOMECHOPT2023.csv");
                    put("Business Option", "BUSOPT2011.csv");
                    put("Software Engineering Option", "SWENGOPT2024.csv");
                    put("Entrepreneurship Option", "ENTROPT2023.csv");
                    put("Artificial Intelligence Option", "AIENGOPT2023.csv");
                    put("Computing Option", "COMPUOPT2024.csv");
                    put("Statistics Option", "STATOPT2023.csv");
                    put("Mathematics Option", "MATHOPT2017.csv");
                    put("Mechatronics Option", "MECTROPT2023.csv");
                }
            };

            List<Audit> audits = new ArrayList<>();

            if (summary != null) {
                // degree
                Resource resource = new ClassPathResource("degree/" + degreeMap.get(summary.programName));
                if (!resource.exists()) {
                    throw new FileNotFoundException("file not found: degree/" + degreeMap.get(summary.programName));
                }

                try (FileReader fileReader = new FileReader(resource.getFile())) {
                    PlanCSVParser parser = new PlanCSVParser();
                    parser.csvIn(fileReader);

                    Audit audit = AuditFactory.getAudit(parser.getPlans().get(0), summary, parser.getLists());

                    audits.add(audit);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }

                // options
                for (String option : summary.optionNames) {
                    Resource optionResource = new ClassPathResource("option/" + optionMap.get(option));
                    if (!optionResource.exists()) {
                        throw new FileNotFoundException("file not found: option/" + optionMap.get(option));
                    }
    
                    try (FileReader fileReader = new FileReader(optionResource.getFile())) {
                        PlanCSVParser parser = new PlanCSVParser();
                        parser.csvIn(fileReader);
    
                        Audit audit = AuditFactory.getAudit(parser.getPlans().get(0), summary, parser.getLists());
    
                        audits.add(audit);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                    }
                }
                
            }

            return ResponseEntity.status(HttpStatus.OK).body(audits);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/whatif")
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
