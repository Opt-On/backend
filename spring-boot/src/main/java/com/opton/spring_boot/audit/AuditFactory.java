package com.opton.spring_boot.audit;

import com.opton.spring_boot.audit.dto.Approval;
import com.opton.spring_boot.audit.dto.Course;
import com.opton.spring_boot.plan.dto.Category;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;
import com.opton.spring_boot.transcript_parser.types.Summary;
import com.opton.spring_boot.transcript_parser.types.TermSummary;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AuditFactory produces an Audit for a given plan and student.
 */
public class AuditFactory {

    /**
     * Generates an audit for the given plan and student summary.
     *
     * @param plan      The academic plan to audit.
     * @param summary   The student's academic summary.
     * @param approvals List of course approvals.
     * @return An Audit object.
     */
    public static Audit getAudit(Plan plan, Summary summary, List<Approval> approvals) {
        // Match courses from the student summary to the requirements in the plan
        Map<Requirement, List<Course>> requirementCourseListMap = matchCoursesToRequirements(plan, summary);
        return new Audit(plan, requirementCourseListMap);
    }

    /**
     * Matches courses from the student's summary to the requirements in the plan.
     *
     * @param plan    The academic plan.
     * @param summary The student's academic summary.
     * @return A map of requirements to matched courses.
     */
    private static Map<Requirement, List<Course>> matchCoursesToRequirements(Plan plan, Summary summary) {
        List<Course> courseList = getStudentCourses(summary);

        // Initialize a map to store matched courses by requirements
        Map<Requirement, List<Course>> requirementCourseListMap = new HashMap<>();

        // Iterate over the plan's requirements and try to match them with the student's courses
        for (Category category : plan.getCategoryList()) {
            for (Requirement requirement : category.getRequirementList()) {
                List<Course> matchedCourses = courseList.stream()
                        .filter(course -> courseMatchesRequirement(course, requirement))
                        .collect(Collectors.toList());

                // Debugging: Print the requirement and matched courses
                System.out.println("Requirement: " + requirement.getSbj_list() + " " + requirement.getCnbr_name());
                System.out.println("Matched Courses: " + matchedCourses);

                requirementCourseListMap.put(requirement, matchedCourses);
            }
        }

        return requirementCourseListMap;
    }

    /**
     * Retrieves the student's courses from the student summary.
     *
     * @param summary The student's academic summary.
     * @return A list of student courses.
     */
    private static List<Course> getStudentCourses(Summary summary) {
        List<Course> courseList = new ArrayList<>();

        // Iterate through each term and extract courses
        for (TermSummary termSummary : summary.termSummaries) {
            for (var courseEntry : termSummary.courses) {
                // Split the course name into subject and number
                String courseName = courseEntry.getKey();
                String[] parts = courseName.split(" ", 2); // Split into subject and the rest
                String subject = parts[0];
                String number = parts.length > 1 ? parts[1] : "";

                // Create a Course object with separate subject and number
                Course course = new Course(subject, number);
                courseList.add(course);
            }
        }

        // Debugging: Print the student's courses
        System.out.println("Student Courses: " + courseList);

        return courseList;
    }

    /**
     * Determines whether a course matches a requirement.
     *
     * @param course      The course to check.
     * @param requirement The requirement to check against.
     * @return True if the course matches the requirement, false otherwise.
     */
    private static boolean courseMatchesRequirement(Course course, Requirement requirement) {
        boolean matches = course.getSbj_list().equals(requirement.getSbj_list()) && 
                          course.getCnbr_name().equals(requirement.getCnbr_name());

        // Debugging: Print the comparison
        System.out.println("Comparing Course: " + course.getSbj_list() + " " + course.getCnbr_name() + 
                           " with Requirement: " + requirement.getSbj_list() + " " + requirement.getCnbr_name() + 
                           " -> " + matches);

        return matches;
    }
}