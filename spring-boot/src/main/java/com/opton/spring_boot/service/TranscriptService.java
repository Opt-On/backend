package com.opton.spring_boot.service;

import org.springframework.stereotype.Service;

import com.google.cloud.firestore.Firestore;
import com.opton.spring_boot.entity.Transcript;
import com.opton.spring_boot.transcript_parser.types.Summary;

@Service
public class TranscriptService {
    private Firestore firestore;

    // constructor injection
    public TranscriptService(Firestore firestore) {
        this.firestore = firestore;
    }

    public void add(Summary summary) {
        System.err.println("adding summary");
        final var transcript = new Transcript();
        transcript.setProgramName(summary.programName);
        transcript.setStudentId(summary.studentNumber);

        firestore.collection(Transcript.ENTITY_NAME).document().set(transcript);
    }
    
}
