package com.opton.spring_boot.transcript_parser.types;

import java.util.ArrayList;
import java.util.Map.Entry;

public class TermSummary {
    // Term ids are numbers of the form 1189 (Fall 2018)
    public int termId;

    // Levels are similar to 1A, 5C (delayed graduation)
    public String level;

    // using map for now, have serialization errors with custom class that i dont wanna deal with
    public ArrayList<Entry<String, String>> courses;
}

