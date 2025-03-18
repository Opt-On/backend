package com.opton.spring_boot.audit;

import com.opton.spring_boot.plan.dto.Category;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;
import com.opton.spring_boot.plan.dto.PlanList;
import com.opton.spring_boot.transcript_parser.types.Summary;
import com.opton.spring_boot.transcript_parser.types.TermSummary;

import java.util.*;

/**
 * AuditFactory produces an Audit for a given plan, student summary, and
 * PlanLists.
 */
public class AuditFactory {

    /**
     * Generates an audit for the given plan, student summary, and PlanLists.
     *
     * @param plan      The academic plan to audit.
     * @param summary   The student's academic summary.
     * @param planLists The list of PlanLists to use for flexible requirements.
     * @return An Audit object.
     */
    public static Audit getAudit(Plan plan, Summary summary, List<PlanList> planLists) {
        // Match courses from the student summary to the requirements in the plan
        Map<Requirement, List<Course>> requirementCourseListMap = matchCoursesToRequirements(plan, summary, planLists);
        return new Audit(plan, requirementCourseListMap);
    }

    /**
     * Matches courses from the student's summary to the requirements in the plan.
     *
     * @param plan      The academic plan.
     * @param summary   The student's academic summary.
     * @param planLists The list of PlanLists to use for flexible requirements.
     * @return A map of requirements to matched courses.
     */
    private static Map<Requirement, List<Course>> matchCoursesToRequirements(Plan plan, Summary summary,
            List<PlanList> planLists) {
        List<Course> courseList = getStudentCourses(summary);

        // Initialize a map to store matched courses by requirements with their
        // priorities
        Map<Requirement, Map<Course, Priority>> requirementCoursePriorityMap = new HashMap<>();

        // Track which courses have already been assigned to a requirement
        Set<Course> assignedCourses = new HashSet<>();

        // Iterate over the plan's requirements and try to match them with the student's
        // courses
        for (Category category : plan.getCategoryList()) {
            for (Requirement requirement : category.getRequirementList()) {
                Map<Course, Priority> matchedCourses = new HashMap<>();

                for (Course course : courseList) {
                    // Skip failed courses and courses already assigned to another requirement
                    if (course.priority == Priority.Failed || assignedCourses.contains(course)) {
                        continue;
                    }

                    // Check if the course matches the requirement (including PlanList requirements)
                    if (courseMatchesRequirement(course, requirement, planLists)) {
                        matchedCourses.put(course, course.priority);
                        assignedCourses.add(course); // Mark the course as assigned
                    }
                }

                requirementCoursePriorityMap.put(requirement, matchedCourses);
            }
        }

        // Solve the constraint satisfaction problem using priorities
        return solveWithPriorities(requirementCoursePriorityMap);
    }

    /**
     * Solves the constraint satisfaction problem using priorities.
     *
     * @param requirementCoursePriorityMap A map of requirements to courses with
     *                                     their priorities.
     * @return A map of requirements to matched courses.
     */
    private static Map<Requirement, List<Course>> solveWithPriorities(
            Map<Requirement, Map<Course, Priority>> requirementCoursePriorityMap) {
        // Initialize the CourseAllocator
        int numCourses = requirementCoursePriorityMap.values().stream()
                .mapToInt(Map::size)
                .sum();
        int numRequirements = requirementCoursePriorityMap.size();
        CourseAllocator allocator = new CourseAllocator(numCourses, numRequirements);

        // Add course-requirement matches to the allocator with their priorities
        int row = 0;
        Map<Integer, Course> rowToCourseMap = new HashMap<>();
        Map<Integer, Requirement> colToRequirementMap = new HashMap<>();
        int col = 0;

        for (Map.Entry<Requirement, Map<Course, Priority>> entry : requirementCoursePriorityMap.entrySet()) {
            Requirement requirement = entry.getKey();
            colToRequirementMap.put(col, requirement);

            for (Map.Entry<Course, Priority> courseEntry : entry.getValue().entrySet()) {
                Course course = courseEntry.getKey();
                Priority priority = courseEntry.getValue();
                rowToCourseMap.put(row, course);

                // Set the match in the allocator with the priority
                allocator.set(row, col, priority.ordinal());
                row++;
            }
            col++;
        }

        // Solve the allocation problem
        allocator.solve();

        // Build the result map
        Map<Requirement, List<Course>> result = new HashMap<>();
        for (int c = 0; c < numRequirements; c++) {
            Requirement requirement = colToRequirementMap.get(c);
            int r = allocator.firstRow(c);
            if (r != -1) {
                Course course = rowToCourseMap.get(r);
                result.computeIfAbsent(requirement, k -> new ArrayList<>()).add(course);
            }
        }

        return result;
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
            for (var c : termSummary.courses) {
                for (Map.Entry<String, String> courseEntry : c.entrySet()) {
                    Course course = new Course(courseEntry.getKey(), courseEntry.getValue());
                    courseList.add(course);
                }
            }
        }

        return courseList;
    }

    /**
     * Normalizes a course code by removing whitespace and converting to uppercase.
     *
     * @param courseCode The course code to normalize.
     * @return The normalized course code.
     */
    private static String normalize(String courseCode) {
        return courseCode.replaceAll("\\s", "").toUpperCase();
    }

    /**
     * Determines whether a course matches a requirement.
     *
     * @param course      The course to check.
     * @param requirement The requirement to check against.
     * @param planLists   The list of PlanLists to use for flexible requirements.
     * @return True if the course matches the requirement, false otherwise.
     */
    private static boolean courseMatchesRequirement(Course course, Requirement requirement, List<PlanList> planLists) {
        String normalizedCourseCode = normalize(course.getSbj_list() + course.getCnbr_name());
        String normalizedRequirementCode = normalize(requirement.getSbj_list() + requirement.getCnbr_name());

        // Check if the requirement is a PlanList
        if (requirement.getSbj_list().equals("list")) {
            String listName = requirement.getCnbr_name();
            PlanList planList = findPlanListByName(listName, planLists);
            if (planList != null) {
                for (com.opton.spring_boot.plan.dto.Course listCourse : planList.getItems()) {
                    String normalizedListCourseCode = normalize(listCourse.getSbj_list() + listCourse.getCnbr_name());
                    if (normalizedCourseCode.equals(normalizedListCourseCode)) {
                        return true;
                    }
                }
            }
            return false;
        }

        // Default case: direct match
        return normalizedCourseCode.equals(normalizedRequirementCode);
    }

    /**
     * Finds a PlanList by its name.
     *
     * @param listName  The name of the PlanList to find.
     * @param planLists The list of PlanLists to search.
     * @return The PlanList object, or null if not found.
     */
    private static PlanList findPlanListByName(String listName, List<PlanList> planLists) {
        if (planLists == null) {
            return null;
        }
        for (PlanList planList : planLists) {
            if (planList.getName().equals(listName)) {
                return planList;
            }
        }
        return null;
    }
}