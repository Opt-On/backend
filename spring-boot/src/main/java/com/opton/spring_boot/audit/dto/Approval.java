package com.opton.spring_boot.audit.dto;

import com.opton.spring_boot.course.dto.Course;
import com.opton.spring_boot.plan.dto.Requirement;

import lombok.Data;

/**
 * Represents a course approval or substitution.
 */
@Data
public class Approval {
    private String subject;
    private String catalogNumber;

    public boolean approves(Course course, Requirement requirement) {
        return course.matches(requirement) && course.getSubjectCode().equals(subject) && course.getCatalog().equals(catalogNumber);
    }
}
