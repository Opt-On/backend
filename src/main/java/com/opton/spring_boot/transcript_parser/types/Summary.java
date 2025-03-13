package com.opton.spring_boot.transcript_parser.types;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Summary {
    // Student number
    public int studentNumber;
    public String studentName;

    // Program name
    public String programName;

    // List of TermSummaries
    public ArrayList<TermSummary> termSummaries;
} 
