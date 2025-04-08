package com.opton.spring_boot.controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.transcript_parser.types.Summary;
import com.opton.spring_boot.transcript_parser.types.TermSummary;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {

    // private static WebClient webClient = WebClient.create("http://3.143.22.149:8008");
    private static WebClient webClient = WebClient.create("http://0.0.0.0:8008");
    private static final Set<String> OPTIONS = Set.of(
        "Artificial Intelligence Option",
        "Biomechanics Option",
        "Computer Engineering Option",
        "Computing Option",
        "Entrepreneurship Option",
        "Environmental Engineering Option",
        "Life Sciences Option",
        "Management Science Option",
        "Management Sciences Option",
        "Mechatronics Option",
        "Physical Sciences Option",
        "Quantum Engineering Option",
        "Software Engineering Option",
        "Statistics Option"
    );

    @Autowired
    private Firestore firestore;

    @CrossOrigin(origins = {"http://localhost:3000", "https://opton.ca"})
    @PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestHeader("email") String email, @RequestHeader("option") String optionName) {
        if (!OPTIONS.contains(optionName)){
            System.out.println(optionName);
            System.err.println("bad option");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            DocumentSnapshot document = firestore.collection("user").document(email).get().get();

            if (!document.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Map<String, Object> requestBody = new HashMap<>();

            Summary summary = document.toObject(Summary.class);
            String term;
            ArrayList<String> courses = new ArrayList<>();
            if (summary != null && summary.termSummaries.size() > 0){
                term = summary.termSummaries.get(summary.termSummaries.size() - 1).level;
                for (TermSummary termSummary: summary.termSummaries){
                    for (Map<String, String> map : termSummary.courses) {
                        for (String key: map.keySet()){
                            if (map.get(key).equals("In Progress") || map.get(key).equals("CR")){
                                courses.add(key);
                            }
                            try {
                                int grade = Integer.parseInt(map.get(key)); // Try converting to an integer
                                if (grade > 50) {
                                    courses.add(key);
                                }
                            } catch (NumberFormatException e) {
                                // Ignore non-integer values
                            }
                        }
                    }
                }
            }
            else{
                term = "1A";
            }

            // hacky fix for MSCI -> MSE
            for (int i = 0; i < courses.size(); i++) {
                courses.set(i, courses.get(i).replace("MSCI", "MSE"));
            }

            requestBody.put("program", summary != null && summary.programName != null ? summary.programName : "null");
            requestBody.put("courses", courses);
            requestBody.put("term", term);
            
            String response = webClient.post()
                .uri("/recommendations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            System.err.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

