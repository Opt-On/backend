package com.opton.spring_boot.controller;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opton.spring_boot.audit.Audit;
import com.opton.spring_boot.audit.AuditFactory;
import com.opton.spring_boot.audit.dto.*;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;
import com.opton.spring_boot.transcript_parser.TranscriptParser;
import com.opton.spring_boot.transcript_parser.types.Summary;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @PostMapping(value = "/degree", consumes = "multipart/form-data")
    public ResponseEntity<Audit> handleDegreeAudit(@RequestParam("transcript") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            Summary summary = TranscriptParser.ParseTranscript(file);
            Plan plan = getPlan();
            Audit audit = AuditFactory.getAudit(plan, summary, new ArrayList<Approval>());

            return ResponseEntity.status(HttpStatus.OK).body(audit);
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/option")
    public ResponseEntity<Audit> handleOptionAudit() {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    private Plan getPlan() {
        Plan plan = new Plan("ME", 2023);

        // 1A Term
        plan.add("1A", new Requirement("CS", "145"));
        plan.add("1A", new Requirement("MATH", "145"));
        plan.add("1A", new Requirement("MATH", "147"));
        plan.add("1A", new Requirement("PYSCH", "101"));
        plan.add("1A", new Requirement("SPCOM", "223"));

        // 1B Term
        plan.add("1B", new Requirement("CS", "146"));
        plan.add("1B", new Requirement("ECE", "142"));
        plan.add("1B", new Requirement("ME", "115"));
        plan.add("1B", new Requirement("ME", "123"));

        // 2A Term
        plan.add("2A", new Requirement("ME", "201"));
        plan.add("2A", new Requirement("ME", "202"));
        plan.add("2A", new Requirement("ME", "219"));
        plan.add("2A", new Requirement("ME", "230"));
        plan.add("2A", new Requirement("ME", "269"));

        // 2B Term
        plan.add("2B", new Requirement("ME", "203"));
        plan.add("2B", new Requirement("ME", "212"));
        plan.add("2B", new Requirement("ME", "220"));
        plan.add("2B", new Requirement("ME", "250"));
        plan.add("2B", new Requirement("ME", "262"));

        // 3A Term
        plan.add("3A", new Requirement("ME", "303"));
        plan.add("3A", new Requirement("ME", "321"));
        plan.add("3A", new Requirement("ME", "340"));
        plan.add("3A", new Requirement("ME", "351"));
        plan.add("3A", new Requirement("ME", "354"));

        // 3B Term
        plan.add("3B", new Requirement("ME", "322"));
        plan.add("3B", new Requirement("ME", "353"));
        plan.add("3B", new Requirement("ME", "360"));
        plan.add("3B", new Requirement("ME", "362"));
        plan.add("3B", new Requirement("ME", "380"));
        plan.add("3B", new Requirement("MSCI", "261"));

        // 4A Term
        plan.add("4A", new Requirement("ME", "481"));

        // 4B Term
        plan.add("4B", new Requirement("ME", "482"));
        return plan;
    }
}
