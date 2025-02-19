package com.opton.spring_boot.audit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseDetails {

    @JsonProperty("career_code")
    private String careerCode;

    @JsonProperty("class_id")
    private short classId;

    @JsonProperty("course_as_of")
    private String courseAsOf;

    @JsonProperty("course_take_units")
    private double courseTakeUnits;

    @JsonProperty("course_title")
    private String courseTitle;

    @JsonProperty("earn_credit")
    private boolean earnCredit;

    @JsonProperty("grade_category")
    private String gradeCategory;

    @JsonProperty("grade_points")
    private double gradePoints;

    @JsonProperty("grading_basis_code")
    private String gradingBasisCode;

    @JsonProperty("grade_submission_date")
    private String gradeSubmissionDate;

    @JsonProperty("group_code")
    private String groupCode;

    @JsonProperty("include_in_gpa")
    private boolean includeInGpa;

    @JsonProperty("requirement_designation")
    private String requirementDesignation;

    @JsonProperty("requirement_designation_grade")
    private String requirementDesignationGrade;

    @JsonProperty("transcript_note")
    private String transcriptNote;

    @JsonProperty("student_take_units")
    private double studentTakeUnits;

    @JsonProperty("units_attempted_code")
    private char unitsAttemptedCode;

    @JsonProperty("student_ap_units")
    private double studentApUnits;

    @JsonProperty("valid_attempt")
    private boolean validAttempt;
}