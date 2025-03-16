package com.opton.spring_boot.transcript_parser.types;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResult {
    String courseName;

    // Course codes are similar to CS 145, STAT 920, PD 1, CHINA 120R
    // special grade values are:
    // INC (Incomplete) - reflects an agreement between a professor and student to hand in work late; no numeric value. Please refer to the INC grade process for further information.
    // FTC (Failure to Complete) - calculated in averages as 32%
    // NMR (No Mark Recorded) - no work submitted for the course, calculated in averages as 32%
    // DNW (Did Not Write) - missed the final exam (calculated in averages as 32%)
    // MM (Missing Mark) - grade has not been received by the Registrar's Office, no numeric value
    // WD (Withdrew) - no numeric value
    // WF (Withdrew too late, Failed) - calculated in averages as 32%
    String grade;
    
    public CourseResult(String courseName, String grade){
        this.courseName = courseName;
        this.grade = grade;
    }

    public CourseResult(){}
}
