package com.opton.spring_boot.course.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.opton.spring_boot.plan.dto.Requirement;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course implements Comparable<Course> {

    private static final String[] LETTER_GRADES = {
        "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-",
        "F+", "F", "F-"
    };
    private static final int[] LETTER_GRADE_VALUES = {
        95, 89, 83, 78, 75, 72, 68, 65, 62, 58, 55, 52, 46, 38, 32
    };

    @JsonProperty("attempt_class")
    private String attemptClass;

    @JsonProperty("catalog")
    private String catalog;

    @JsonProperty("course_grade")
    private String courseGrade;

    @JsonProperty("course_id")
    private short courseId;

    @JsonProperty("details")
    private CourseDetails details = new CourseDetails();

    @JsonProperty("is_academic_subject")
    private boolean isAcademicSubject;

    @JsonProperty("subject_code")
    private String subjectCode;

    @JsonProperty("term_id")
    private short termId;

    @JsonProperty("transfer_course")
    private boolean transferCourse;

    @JsonProperty("uw_id")
    private int studentNumber;

    private String instructor = "";
    private String section = "";

    public Course(String subject, String catalogNumber) {
        this.subjectCode = subject;
        this.catalog = catalogNumber;
        this.termId = 0;
        this.details.setClassId((short) 0);
        this.details.setCourseTitle("");
        this.details.setCourseTakeUnits(0.0);
        this.details.setGradingBasisCode("");
        this.courseGrade = "";
        this.details.setRequirementDesignation("");
        this.details.setRequirementDesignationGrade("");
    }

    @Override
    public int compareTo(Course other) {
        return Comparator.comparing(Course::getSubjectCode)
                .thenComparing(Course::getCatalog)
                .thenComparingInt(Course::getTermId)
                .thenComparingInt(o -> o.getDetails().getClassId())
                .compare(this, other);
    }

    public boolean valid() {
        return (inProgress() || pass()) && inDegree() && getUnits() > 0;
    }

    public boolean pass() {
        String gradingBasis = getGradingBasis();
        String grade = getCourseGrade();

        if ("NUM".equals(gradingBasis) || "NGP".equals(gradingBasis)) {
            return getIntGrade() >= 50;
        } else if ("ABC".equals(gradingBasis)) {
            for (String passGrade : LETTER_GRADES) {
                if (passGrade.equals(grade)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean inProgress() {
        String grade = getCourseGrade();
        return "".equals(grade) || "INC".equals(grade) || "IP".equals(grade);
    }

    public boolean inDegree() {
        String reqDesignation = getRequirementDesignation();
        return !("NRNA".equals(reqDesignation) || "TRIA".equals(reqDesignation));
    }

    public int getIntGrade() {
        try {
            return Integer.parseInt(getCourseGrade());
        } catch (NumberFormatException e) {
            for (int i = 0; i < LETTER_GRADES.length; i++) {
                if (LETTER_GRADES[i].equals(getCourseGrade())) {
                    return LETTER_GRADE_VALUES[i];
                }
            }
        }
        return 0;
    }

    public String getGradingBasis() {
        return details.getGradingBasisCode() != null ? details.getGradingBasisCode() : "";
    }

    public String getRequirementDesignation() {
        return details.getRequirementDesignation() != null ? details.getRequirementDesignation() : "";
    }

    public String getRequirementDesignationGrade() {
        return details.getRequirementDesignationGrade() != null ? details.getRequirementDesignationGrade() : "";
    }

    public double getUnits() {
        return details.getStudentTakeUnits();
    }

    @Override
    public String toString() {
        return String.format("%s%s/%d/%s %s %s",
            getSubjectCode(), getCatalog(), getTermId(), getCourseGrade(),
            getRequirementDesignation(), getRequirementDesignationGrade());
    }
    
    public boolean matches(Requirement requirement) {
        return this.subjectCode.equals(requirement.getSbj_list()) &&
            this.catalog.equals(requirement.getCnbr_name());
    }

}
