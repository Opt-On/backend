package com.opton.spring_boot.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.audit.Audit;
import com.opton.spring_boot.audit.AuditFactory;
import com.opton.spring_boot.plan.PlanCSVParser;
import com.opton.spring_boot.transcript_parser.types.Summary;
import com.opton.spring_boot.transcript_parser.types.TermSummary;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @Autowired
    private Firestore firestore;

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/declared")
    public ResponseEntity<List<Audit>> handleDeclaredAudit(@RequestHeader("email") String email) {
        try {
            DocumentSnapshot document = firestore.collection("user").document(email).get().get();
            if (!document.exists())
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

            Summary summary = document.toObject(Summary.class);

            String year = "";
            if (summary != null) {
                for (TermSummary termSummary : summary.termSummaries) {
                    if (termSummary.level.equals("1A")) { 
                        String yearString = Integer.toString(termSummary.termId);
                        year = "20" + yearString.substring(1, yearString.length() - 1);
                    }
                }
            }
            
            Map<String, List<String>> degreeMap = new HashMap<>() {
                {
                    put("Aerospace Engineering", List.of("AE2018.csv"));
                    put("Biomedical Engineering", List.of("BIOMEDE2017.csv"));
                    put("Chemical Engineering", List.of("CHE2020.csv", "CHE2021.csv", "CHE2022.csv", "CHE2023.csv"));
                    put("Computer Engineering", List.of("COMPE2020.csv", "COMPE2021.csv", "COMPE2022.csv", "COMPE2023.csv"));
                    put("Electrical Engineering", List.of("ELE2020.csv", "ELE2021.csv", "ELE2022.csv", "ELE2023.csv"));
                    put("Mechanical Engineering", List.of("ME2020.csv", "ME2021.csv", "ME2022.csv", "ME2023.csv"));
                    put("Mechatronics Engineering", List.of("MECTR2020.csv", "MECTR2021.csv", "MECTR2022.csv", "MECTR2023.csv"));
                    put("Management Engineering", List.of("MGTE2020.csv", "MGTE2021.csv", "MGTE2022.csv", "MGTE2023.csv"));
                    put("Nanotechnology Engineering", List.of("NE2018.csv"));
                    put("Software Engineering", List.of("SE2020.csv", "SE2021.csv", "SE2022.csv", "SE2023.csv"));
                    put("Architectural Engineering", List.of("ARCHPPENG2017.csv"));
                    put("Civil Engineering", List.of("CIVE2017.csv"));
                    put("Environmental Engineering", List.of("ENVE2017.csv"));
                    put("Geological Engineering", List.of("GEOE2017.csv"));
                    put("Systems Design Engineering", List.of("SYDE2017.csv"));
                }
            };

            Map<String, String> optionMap = new HashMap<String, String>() {
                {
                    put("Cognitive Science Option", "COGSCOPT2012.csv");
                    put("Computer Engineering Option", "COMPENGOPT2024.csv");
                    put("Management Science Option", "MSCIOPT2023.csv");
                    put("Management Sciences Option", "MSCIOPT2023.csv");
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
                String degreeFile = getRelevantDegreeFile(degreeMap, summary.programName, year);

                if (degreeFile == null) {
                    throw new FileNotFoundException("no valid file found: " + summary.programName);
                }

                Resource resource = new ClassPathResource("degree/" + degreeFile);
                if (!resource.exists()) {
                    throw new FileNotFoundException("file not found: degree/" + degreeFile);
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
                        System.out.println("option" + option);
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

    private String getRelevantDegreeFile(Map<String, List<String>> degreeMap, String programName, String year) {
        List<String> files = degreeMap.get(programName);
        if (files == null || files.isEmpty()) return null;
    
        List<String> sortedFiles = files.stream()
                .sorted(Comparator.comparingInt(f -> Integer.parseInt(f.replaceAll("\\D+", ""))))
                .collect(Collectors.toList());
    
        if (year.isEmpty()) {
            return sortedFiles.get(sortedFiles.size() - 1);
        }
    
        int yearInt = Integer.parseInt(year);
        for (String file : sortedFiles) {
            int fileYear = Integer.parseInt(file.replaceAll("\\D+", ""));
            if (fileYear <= yearInt) {
                return file; 
            }
        }
    
        return sortedFiles.get(sortedFiles.size() - 1);
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
                    put("COMPENGOPT", "COMPENGOPT2024.csv");
                    put("MSCIOPT", "MSCIOPT2023.csv");
                    put("BIOMECHOPT", "BIOMECHOPT2023.csv");
                    put("SWENGOPT", "SWENGOPT2024.csv");
                    put("ENTROPT", "ENTROPT2023.csv");
                    put("AIENGOPT", "AIENGOPT2023.csv");
                    put("COMPUOPT", "COMPUOPT2024.csv");
                    put("STATOPT", "STATOPT2023.csv");
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

    @CrossOrigin(origins = { "http://localhost:3000", "https://opton.ca" })
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

            Map<String, String> optionMap = new HashMap<String, String>() {
                {
                    put("COMPENGOPT", "COMPENGOPT2024.csv");
                    put("MSCIOPT", "MSCIOPT2023.csv");
                    put("BIOMECHOPT", "BIOMECHOPT2023.csv");
                    put("SWENGOPT", "SWENGOPT2024.csv");
                    put("ENTROPT", "ENTROPT2023.csv");
                    put("AIENGOPT", "AIENGOPT2023.csv");
                    put("COMPUOPT", "COMPUOPT2024.csv");
                    put("STATOPT", "STATOPT2023.csv");
                    put("MECTROPT", "MECTROPT2023.csv");
                }
            };

            for (Map.Entry<String, String> entry : optionMap.entrySet()) {
                String fileName = entry.getValue();
                File file = new File(resource.getPath() + "/" + fileName);
                if (file.exists()) {
                    try (FileReader fileReader = new FileReader(file)) {
                        PlanCSVParser parser = new PlanCSVParser();
                        parser.csvIn(fileReader);
                        Audit audit = AuditFactory.getAudit(parser.getPlans().get(0), summary, parser.getLists());
                        double[] scores = audit.calculateProgress();
                        auditMap.put(audit, scores);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                } else {
                    System.err.println("file not found: " + fileName + entry);
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
