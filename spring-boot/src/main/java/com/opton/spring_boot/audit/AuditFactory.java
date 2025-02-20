package com.opton.spring_boot.audit;

import com.opton.spring_boot.audit.dto.Approval;
import com.opton.spring_boot.audit.dto.Course;
import com.opton.spring_boot.plan.dto.ListItem;
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
     * Defines the priority of a course in matching a requirement.
     * Highest is "Approved," which means the course is from manually entered approvals.
     * Next is "Passed," followed by "InProgress."
     */
    private enum Priority { InProgress, Passed, Approved }

    // Mock data for corequisites
    private static final Map<String, List<Course>> MOCK_COREQ_MAP = new HashMap<>();
    static {
        MOCK_COREQ_MAP.put("MATH101", Arrays.asList(new Course("MATH101", "A"), new Course("MATH102", "B")));
        MOCK_COREQ_MAP.put("PHYS101", Arrays.asList(new Course("PHYS101", "A"), new Course("PHYS102", "B")));
    }

    // Mock data for course equivalents
    private static final Map<String, List<Course>> MOCK_EQUIV_MAP = new HashMap<>();
    static {
        MOCK_EQUIV_MAP.put("MATH101", Arrays.asList(new Course("MATH101", "A"), new Course("MATH101X", "A")));
        MOCK_EQUIV_MAP.put("PHYS101", Arrays.asList(new Course("PHYS101", "A"), new Course("PHYS101X", "A")));
    }

    // Mock data for list matches
    private static final Map<String, Set<ListItem>> MOCK_LIST_MATCHES = new HashMap<>();
    static {
        MOCK_LIST_MATCHES.put("MATH101", new HashSet<>(Arrays.asList(new ListItem("MATH", "101"))));
        MOCK_LIST_MATCHES.put("PHYS101", new HashSet<>(Arrays.asList(new ListItem("PHYS", "101"))));
    }

    /**
     * Generates an audit for the given plan and student summary.
     *
     * @param plan      The academic plan to audit.
     * @param summary   The student's academic summary.
     * @param approvals List of course approvals.
     * @return An Audit object.
     */
    public static Audit getAudit(Plan plan, Summary summary, List<Approval> approvals) {
        return new Audit(plan, matchCoursesToRequirements(plan, summary, approvals));
    }

    /**
     * Matches courses to plan requirements.
     *
     * @param plan      The academic plan.
     * @param summary   The student's academic summary.
     * @param approvals List of course approvals.
     * @return A map of requirements to matched courses.
     */
    private static Map<Requirement, List<Course>> matchCoursesToRequirements(Plan plan, Summary summary, List<Approval> approvals) {
        Map<Course, Map<Requirement, Priority>> courseRequirementMap = new TreeMap<>();
        List<Course> courseList = getStudentCourses(summary);
    
        // Remove invalid courses
        courseList.removeIf(course -> !course.valid());
    
        // Merge corequisites using mock data
        Map<String, List<Course>> coreqMap = moveCoursesToCoreqMap(plan.getYear(), courseList);
    
        // Duplicate and filter approvals
        List<Approval> approvalList = duplicateApprovals(approvals, plan);
    
        // Convert studentNumber to String
        String studentNumberStr = String.valueOf(summary.studentNumber);
    
        // Handle plan-approved double counts
        planDoubleCountsToApprovals(approvalList, studentNumberStr, plan);
    
        // Handle multiple approvals
        coreqMap.putAll(moveApprovalCoreqCoursesToCoreqMap(courseList, approvalList));
        coreqMap.putAll(duplicateMultiApprovedCourses(courseList, approvalList));
    
        // Match courses to requirements
        for (int i = 0; i < courseList.size(); i++) {
            Course course = courseList.get(i);
            Map<Requirement, Priority> requirementPriorityMap = getRequirementPriorityMap(course, approvalList, plan);
    
            if (requirementPriorityMap.isEmpty() && isCoreqList(course, coreqMap)) {
                // Replace unmatched coreq list with its constituent courses
                courseList.remove(i--);
                courseList.addAll(coreqMap.get(course.getCatalog()));
            } else {
                courseRequirementMap.put(course, requirementPriorityMap);
            }
        }
    
        return solve(courseRequirementMap, coreqMap, plan);
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
                // Assuming Course can be constructed from courseEntry
                Course course = new Course(
                    courseEntry.getKey(), // Course code (e.g., "MATH101")
                    courseEntry.getValue() // Course grade (e.g., "A")
                );
                courseList.add(course);
            }
        }

        return courseList;
    }

    /**
     * Determines the priority of a course match.
     *
     * @param course The course being matched.
     * @param equiv  The equivalent course.
     * @return The priority of the match.
     */
    private static Priority getPriority(Course course, Course equiv) {
        if (equiv != course) {
            return Priority.Approved;
        } else if (course.pass()) {
            return Priority.Passed;
        } else {
            return Priority.InProgress;
        }
    }

    /**
     * Checks if a course is a corequisite list.
     *
     * @param course   The course to check.
     * @param coreqMap The map of corequisite lists.
     * @return True if the course is a corequisite list, false otherwise.
     */
    private static boolean isCoreqList(Course course, Map<String, List<Course>> coreqMap) {
        return course.getSubjectCode().equals("list") && coreqMap.containsKey(course.getCatalog());
    }

    /**
     * Gets the requirement priority map for a course.
     *
     * @param course       The course to match.
     * @param approvalList The list of approvals.
     * @param plan         The academic plan.
     * @return A map of requirements to their priority.
     */
    private static Map<Requirement, Priority> getRequirementPriorityMap(Course course, List<Approval> approvalList, Plan plan) {
        Map<Requirement, Priority> requirementPriorityMap = new TreeMap<>();
        List<Course> courseEquivList = getCourseEquivalents(course, approvalList);

        for (Course equiv : courseEquivList) {
            Priority priority = getPriority(course, equiv);
            Set<ListItem> listMatchesSet = getListMatchesSet(plan.getYear(), equiv.getSubjectCode(), equiv.getCatalog());
            listMatchesSet.add(new ListItem(equiv.getSubjectCode(), equiv.getCatalog()));

            for (ListItem item : listMatchesSet) {
                Set<Requirement> rqmtSet = getRequirementMatchesSet(item, plan);
                for (Requirement rqmt : rqmtSet) {
                    requirementPriorityMap.put(rqmt, priority);
                }
            }
        }

        return requirementPriorityMap;
    }

    /**
     * Gets the list of equivalent courses for a given course.
     *
     * @param course       The course to find equivalents for.
     * @param approvalList The list of approvals.
     * @return A list of equivalent courses.
     */
    private static List<Course> getCourseEquivalents(Course course, List<Approval> approvalList) {
        List<Course> courseEquivList = new ArrayList<>();
        courseEquivList.add(course); // The original course should be first
        courseEquivList.addAll(getApprovedSubstList(course, approvalList));
        return courseEquivList;
    }

    /**
     * Solves for a maximum covering of requirements by courses.
     *
     * @param courseRequirementMap The map of courses to their matched requirements.
     * @param coreqMap      The map of corequisite lists.
     * @param plan          The academic plan.
     * @return A map of requirements to their matched courses.
     */
    private static Map<Requirement, List<Course>> solve(
        Map<Course, Map<Requirement, Priority>> courseRequirementMap,
        Map<String, List<Course>> coreqMap,
        Plan plan
    ) {
        List<Requirement> rqmtList = plan.getCategoryList().stream()
                .flatMap(category -> category.getRequirementList().stream())
                .collect(Collectors.toList());

        // Initialize the CourseAllocator
        CourseAllocator allocator = new CourseAllocator(courseRequirementMap.size(), rqmtList.size());

        // Populate the allocator with course-requirement priorities
        int courseIndex = 0;
        for (Map<Requirement, Priority> rpMap : courseRequirementMap.values()) {
            for (Map.Entry<Requirement, Priority> rpEntry : rpMap.entrySet()) {
                int rqmtIndex = rqmtList.indexOf(rpEntry.getKey());
                int priority = rpEntry.getValue().ordinal();
                allocator.set(courseIndex, rqmtIndex, priority);
            }
            courseIndex++;
        }

        // Solve the allocation problem
        allocator.solve();

        // Build the solution map
        Map<Requirement, List<Course>> solution = new TreeMap<>();
        Course[] courseArray = courseRequirementMap.keySet().toArray(new Course[0]);
        for (int rqmtIndex = 0; rqmtIndex < rqmtList.size(); rqmtIndex++) {
            List<Course> courseList = new ArrayList<>();
            int courseIndexMatch = allocator.firstRow(rqmtIndex);

            // Check if a valid course index was found
            if (courseIndexMatch >= 0 && courseIndexMatch < courseArray.length) {
                Course course = courseArray[courseIndexMatch];
                if (isCoreqList(course, coreqMap)) {
                    courseList.addAll(coreqMap.get(course.getCatalog()));
                } else {
                    courseList.add(course);
                }
            }

            // Add the requirement and its matched courses (if any) to the solution map
            solution.put(rqmtList.get(rqmtIndex), courseList);
        }

        return solution;
    }

    /**
     * Moves courses to the corequisite map using mock data.
     *
     * @param year       The academic year.
     * @param courseList The list of courses.
     * @return A map of corequisite lists.
     */
    private static Map<String, List<Course>> moveCoursesToCoreqMap(int year, List<Course> courseList) {
        Map<String, List<Course>> coreqMap = new HashMap<>();
        for (Course course : courseList) {
            if (MOCK_COREQ_MAP.containsKey(course.getCatalog())) {
                coreqMap.put(course.getCatalog(), MOCK_COREQ_MAP.get(course.getCatalog()));
            }
        }
        return coreqMap;
    }

    /**
     * Gets the list matches set using mock data.
     *
     * @param year        The academic year.
     * @param subjectCode The subject code.
     * @param catalog     The catalog number.
     * @return A set of list items.
     */
    private static Set<ListItem> getListMatchesSet(int year, String subjectCode, String catalog) {
        return MOCK_LIST_MATCHES.getOrDefault(catalog, new HashSet<>());
    }

    /**
     * Gets the requirement matches set.
     *
     * @param item The list item.
     * @param plan The academic plan.
     * @return A set of requirements.
     */
    private static Set<Requirement> getRequirementMatchesSet(ListItem item, Plan plan) {
        Set<Requirement> rqmtSet = new HashSet<>();
        for (var category : plan.getCategoryList()) {
            for (var rqmt : category.getRequirementList()) {
                if (rqmt.matches(item)) {
                    rqmtSet.add(rqmt);
                }
            }
        }
        return rqmtSet;
    }

    /**
     * Duplicates approvals using mock data.
     *
     * @param approvals The list of approvals.
     * @param plan      The academic plan.
     * @return A list of duplicated approvals.
     */
    private static List<Approval> duplicateApprovals(List<Approval> approvals, Plan plan) {
        return new ArrayList<>(approvals);
    }

    /**
     * Handles plan-approved double counts using mock data.
     *
     * @param approvals     The list of approvals.
     * @param studentNumber The student number.
     * @param plan          The academic plan.
     */
    private static void planDoubleCountsToApprovals(List<Approval> approvals, String studentNumber, Plan plan) {
        // Mock implementation
    }

    /**
     * Moves approval corequisite courses to the corequisite map using mock data.
     *
     * @param courseList   The list of courses.
     * @param approvalList The list of approvals.
     * @return A map of corequisite lists.
     */
    private static Map<String, List<Course>> moveApprovalCoreqCoursesToCoreqMap(List<Course> courseList, List<Approval> approvalList) {
        return new HashMap<>();
    }

    /**
     * Duplicates multi-approved courses using mock data.
     *
     * @param courseList   The list of courses.
     * @param approvalList The list of approvals.
     * @return A map of corequisite lists.
     */
    private static Map<String, List<Course>> duplicateMultiApprovedCourses(List<Course> courseList, List<Approval> approvalList) {
        return new HashMap<>();
    }

    /**
     * Gets the approved substitution list using mock data.
     *
     * @param course       The course.
     * @param approvalList The list of approvals.
     * @return A list of approved substitutions.
     */
    private static List<Course> getApprovedSubstList(Course course, List<Approval> approvalList) {
        return MOCK_EQUIV_MAP.getOrDefault(course.getCatalog(), new ArrayList<>());
    }
}