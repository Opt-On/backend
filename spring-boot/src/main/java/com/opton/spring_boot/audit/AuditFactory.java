package com.opton.spring_boot.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.opton.spring_boot.audit.dto.Approval;
import com.opton.spring_boot.course.dto.Course;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to generate an Audit for a given plan and courses.
 */
@Service
@RequiredArgsConstructor
public class AuditFactory {

    /**
     * Generate an Audit based on the provided plan, courses, and approvals.
     *
     * @param plan       The academic plan to audit.
     * @param courses Student data containing course information.
     * @param approvals  List of course approvals/substitutions.
     * @return An Audit object containing the audit results.
     */
    public Audit generateAudit(Plan plan, List<Course> courses, List<Approval> approvals) {
        Map<Requirement, List<Course>> requirementCourseListMap = matchCoursesToRequirements(plan, courses, approvals);
        return new Audit(plan, requirementCourseListMap);
    }

    /**
     * Match courses to the plan requirements.
     *
     * @param plan        The academic plan to audit.
     * @param courses The student's course data.
     * @param approvals   List of course approvals/substitutions.
     * @return A mapping of requirements to courses that fulfill them.
     */
    private Map<Requirement, List<Course>> matchCoursesToRequirements(Plan plan, List<Course> courses, List<Approval> approvals) {
        return plan.getCategoryList().stream()
        .flatMap(category -> category.getRequirementList().stream())
        .collect(Collectors.toMap(
                requirement -> requirement,
                requirement -> courses.stream() 
                        .filter(course -> matchesRequirement(course, requirement, approvals))
                        .collect(Collectors.toList()) 
        ));
    }

    /**
     * Check if a course matches a given requirement.
     *
     * @param course    The course to check.
     * @param requirement      The requirement to match against.
     * @param approvals List of course approvals.
     * @return True if the course matches the requirement.
     */
    private boolean matchesRequirement(Course course, Requirement requirement, List<Approval> approvals) {
        // Check if the course matches directly or via approvals
        return course.matches(requirement) || approvals.stream()
                .anyMatch(approval -> approval.approves(course, requirement));
    }
}
