package com.opton.spring_boot.transcript_parser.types;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Summary {
    public int studentNumber;
    public String firstName;
    public String lastName;
    public String programName;
    public ArrayList<String> optionNames;
    public String uploadDate;
    public ArrayList<TermSummary> termSummaries;
} 
