package com.opton.spring_boot.controller;

import java.util.AbstractMap;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.opton.spring_boot.audit.Audit;
import com.opton.spring_boot.audit.AuditFactory;
import com.opton.spring_boot.audit.dto.*;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;
import com.opton.spring_boot.transcript_parser.types.Summary;
import com.opton.spring_boot.transcript_parser.types.TermSummary;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @GetMapping("/degree")
    public ResponseEntity<Audit> handleDegreeAudit() {
        try {            
            Plan plan = getPlan();
            Summary summary = getSummary();
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

    private Summary getSummary() {
        Summary summary = new Summary();
    
        summary.studentNumber = 123456;
        summary.studentName = "John Doe";
        summary.programName = "Computer Science";
    
        summary.termSummaries = new ArrayList<>();
    
        // Create a TermSummary object for Fall 2018
        TermSummary term1 = new TermSummary();
        term1.termId = 1189;
        term1.level = "1A";
        term1.courses = new ArrayList<>();
        term1.courses.add(new AbstractMap.SimpleEntry<>("CS 145", "Introduction to Programming")); // Matches CS 145
        term1.courses.add(new AbstractMap.SimpleEntry<>("MATH 145", "Linear Algebra")); // Matches MATH 145
        term1.courses.add(new AbstractMap.SimpleEntry<>("MATH 147", "Calculus 1")); // Matches MATH 147
        term1.courses.add(new AbstractMap.SimpleEntry<>("PSYCH 101", "Introduction to Psychology")); // Matches PSYCH 101
        term1.courses.add(new AbstractMap.SimpleEntry<>("SPCOM 223", "Public Speaking")); // Matches SPCOM 223
    
        // Create a TermSummary object for Winter 2019
        TermSummary term2 = new TermSummary();
        term2.termId = 1191;
        term2.level = "1B";
        term2.courses = new ArrayList<>();
        term2.courses.add(new AbstractMap.SimpleEntry<>("CS 146", "Data Structures and Algorithms")); // Matches CS 146
        term2.courses.add(new AbstractMap.SimpleEntry<>("ECE 142", "Digital Circuits")); // Matches ECE 142
        term2.courses.add(new AbstractMap.SimpleEntry<>("ME 115", "Engineering Graphics")); // Matches ME 115
        term2.courses.add(new AbstractMap.SimpleEntry<>("ME 123", "Statics")); // Matches ME 123
    
        // Add TermSummary objects to the list
        summary.termSummaries.add(term1);
        summary.termSummaries.add(term2);
    
        return summary;
    }

    private Plan getPlan() {
        Plan plan = new Plan("ME", 2023);

        // 1A Term
        plan.add("1A", new Requirement("CS", "145"));
        plan.add("1A", new Requirement("MATH", "145"));
        plan.add("1A", new Requirement("MATH", "147"));
        plan.add("1A", new Requirement("PSYCH", "101"));
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
