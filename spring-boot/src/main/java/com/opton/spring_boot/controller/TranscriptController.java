package com.opton.spring_boot.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // TODO: Add dependency injection for transcript service using @AutoWired <- maybe schizo message
    private final TranscriptService transcriptService;

    public TranscriptController(TranscriptService transcriptService){
        this.transcriptService = transcriptService;
    }
    
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "false") boolean includeGrade) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        try {
            Summary summary = TranscriptParser.ParseTranscript(file);
            transcriptService.setTranscript(summary, includeGrade);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(){
        try{
            Summary summary = transcriptService.getTranscript(20834749);
            return ResponseEntity.status(200).body(summary.studentName); 
        }
        catch (Exception e){
            return ResponseEntity.status(200).body("ok");
        }
    }
}
