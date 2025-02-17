package com.opton.spring_boot.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.transcript_parser.types.Summary;

@Service
public class TranscriptService {
    private final Firestore firestore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // // constructor injection
    public TranscriptService(Firestore firestore) {
        this.firestore = firestore;
    }

    public String getProgram(int userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("user").document(String.valueOf(userId));
        DocumentSnapshot document = docRef.get().get(); // Blocking call, consider async

        if (document.exists() && document.contains("programName")) {
            return document.getString("programName");
        } else {
            return "Program Name not found";
        }
    }

    @Async
    public CompletableFuture<String> setProgram(Summary summary) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentReference docRef = firestore.collection("user").document(String.valueOf(summary.studentNumber));
                
                @SuppressWarnings("unchecked")
                Map<String, Object> userJson = objectMapper.convertValue(summary, Map.class);

                docRef.set(userJson).get();
                return "Program Name set successfully";
            } catch (ExecutionException | InterruptedException e) {
                Thread.currentThread().interrupt();
                return "Error setting Program Name";
            }
        });
    }
    
}
