package com.opton.spring_boot.controller;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

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

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestParam("email") String email, @RequestParam("option") String optionName) {
        if (OPTIONS.contains(optionName)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            // map program to code
            // parse term
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

            System.out.println("response");
            System.out.println(response);
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

