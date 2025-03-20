package com.opton.spring_boot.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.transcript_parser.types.Summary;

@RestController
@RequestMapping("/recommendation")
public class RecommendationController {
    private static WebClient webClient = WebClient.create("http://18.222.23.64:8008");
    private static final Set<String> OPTIONS = Set.of(
        "Artificial Intelligence Option",
        "Biomechanics Option",
        "Computer Engineering Option",
        "Computing Option",
        "Entrepreneurship Option",
        "Environmental Engineering Option",
        "Life Sciences Option",
        "Management Science Option",
        "Mechatronics Option",
        "Physical Sciences Option",
        "Quantum Engineering Option",
        "Software Engineering Option",
        "Statistics Option"
    );

    // private final Firestore firestore;

    // // constructor injection
    // public RecommendationController(Firestore firestore) {
    //     this.firestore = firestore;
    // }

    @Autowired
    private Firestore firestore;

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestParam("email") String email, @RequestParam("option") String optionName) {
        if (OPTIONS.contains(optionName)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            DocumentSnapshot document = firestore.collection("user").document(email).get().get();

            if (!document.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            System.err.println(document.toObject(Summary.class));

            Map<String, Object> requestBody = new HashMap<>();
            // todo: 
            // map program to code
            // parse term
            // get all passed/ inprogress courses
            requestBody.put("program", "CHEM");
            requestBody.put("courses", List.of("CS101", "CS102", "MATH201"));
            requestBody.put("term", "1A");


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

