package com.opton.transcript_parser.types;
import java.util.ArrayList;

public class TermSummary {
    // Term ids are numbers of the form 1189 (Fall 2018)
    public int termId;

    // Levels are similar to 1A, 5C (delayed graduation)
    public String level;

    // Course codes are similar to CS 145, STAT 920, PD 1, CHINA 120R
    public ArrayList<String> courses;
}
