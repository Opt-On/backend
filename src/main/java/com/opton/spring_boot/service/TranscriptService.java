package com.opton.spring_boot.service;

import java.util.ArrayList;
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

    public Summary getTranscript(int userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("user").document(String.valueOf(userId));
        DocumentSnapshot document = docRef.get().get(); // Blocking call, consider async

        if (document.exists()) {
            return document.toObject(Summary.class);
        } else {
            return null;
        }
    }

    @Async
    public CompletableFuture<String> setTranscript(Summary summary, Boolean includeGrade) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DocumentReference docRef = firestore.collection("user").document(String.valueOf(summary.studentNumber));
                
                // bypass adding grade info
                if (!includeGrade){
                    summary.termSummaries = new ArrayList<>();
                }

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

    // synchronous firestore upload for debug, might wanna switch to this later to handle upload errors
    // public String setTranscript(Summary summary) throws InterruptedException, ExecutionException {
    //     DocumentReference docRef = firestore.collection("user").document(String.valueOf(summary.studentNumber));
        
    //     @SuppressWarnings("unchecked")
    //     Map<String, Object> userJson = objectMapper.convertValue(summary, Map.class);

        
    //     docRef.set(userJson).get();
    //     return "Program Name set successfully";

    // }
    
}
