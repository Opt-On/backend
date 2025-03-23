package com.opton.spring_boot.controller;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opton.spring_boot.service.TranscriptService;
import com.opton.spring_boot.transcript_parser.TranscriptParser;
import com.opton.spring_boot.transcript_parser.types.Summary;

@RestController
@RequestMapping("/transcript")
public class TranscriptController {

    @Autowired
    private final TranscriptService transcriptService;

    public TranscriptController(TranscriptService transcriptService){
        this.transcriptService = transcriptService;
    }
    
    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "true") boolean includeGrade, @RequestParam("email") String email) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            Summary summary = TranscriptParser.ParseTranscript(file);
            transcriptService.setTranscript(summary, includeGrade, email);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/upload/text")
    public ResponseEntity<String> handleTextTranscriptUpload(@RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "true") boolean includeGrade, @RequestParam("email") String email) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            String text = new String(file.getBytes(), StandardCharsets.UTF_8);
            Summary summary = TranscriptParser.ParseTranscriptText(text);
            transcriptService.setTranscript(summary, includeGrade, email);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @CrossOrigin
    @GetMapping("/test")
    public ResponseEntity<String> test(){
        try{
            Summary summary = transcriptService.getTranscript(20834749);
            return ResponseEntity.status(200).body(summary.firstName); 
        }
        catch (Exception e){
            return ResponseEntity.status(200).body("ok");
        }
    }
}
