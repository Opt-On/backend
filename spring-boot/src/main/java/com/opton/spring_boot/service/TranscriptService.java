package com.opton.spring_boot.service;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.entity.Transcript;
import com.opton.spring_boot.transcript_parser.types.Summary;

@Service
public class TranscriptService {
    private final Firestore firestore;

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

    public void add(Summary summary) {
        System.err.println("adding summary");
        final var transcript = new Transcript();
        transcript.setProgramName("summary");
        transcript.setStudentId(1234);

        firestore.collection(Transcript.ENTITY_NAME).document().set(transcript);
    }
    
}
