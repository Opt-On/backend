package com.opton.spring_boot.audit;

import com.opton.spring_boot.plan.dto.Category;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;
import com.opton.spring_boot.plan.dto.PlanList;
import com.opton.spring_boot.transcript_parser.types.Summary;
import com.opton.spring_boot.transcript_parser.types.TermSummary;

import java.util.*;

public class AuditFactory {

    public static Audit getAudit(Plan plan, Summary summary, List<PlanList> planLists, Map<Course, Integer> courseUsageMap) {
        Map<Requirement, List<Course>> requirementCourseListMap = matchCoursesToRequirements(plan, summary, planLists, courseUsageMap);
        return new Audit(plan, requirementCourseListMap, courseUsageMap);
    }

    private static Map<Requirement, Integer> countRequirementOccurrences(Plan plan) {
        Map<Requirement, Integer> requirementCountMap = new HashMap<>();
        for (Category category : plan.getCategoryList()) {
            for (Requirement requirement : category.getRequirementList()) {
                requirementCountMap.put(requirement, requirementCountMap.getOrDefault(requirement, 0) + 1);
            }
        }
        return requirementCountMap;
    }

    private static Map<Requirement, List<Course>> matchCoursesToRequirements(Plan plan, Summary summary,
            List<PlanList> planLists, Map<Course, Integer> courseUsageMap) {
        List<Course> courseList = getStudentCourses(summary);
        Map<Requirement, List<Course>> requirementCourseMap = new HashMap<>();
        Set<Course> locallyAssignedCourses = new HashSet<>();
        Map<Requirement, Integer> requirementCountMap = countRequirementOccurrences(plan);

        for (Category category : plan.getCategoryList()) {
            for (Requirement requirement : category.getRequirementList()) {
                List<Course> coursesForRequirement = requirementCourseMap.computeIfAbsent(requirement,
                        k -> new ArrayList<>());

                if (coursesForRequirement.size() >= requirementCountMap.get(requirement)) {
                    continue;
                }

                List<Course> matchingCourses = new ArrayList<>();
                for (Course course : courseList) {
                    if (course.priority == Priority.Failed || locallyAssignedCourses.contains(course) || 
                        courseUsageMap.getOrDefault(course, 0) >= 2) {
                        continue;
                    }

                    if (courseMatchesRequirement(course, requirement, planLists)) {
                        matchingCourses.add(course);
                    }
                }

                matchingCourses.sort(Comparator.comparing(Course::getPriority).reversed());

                for (Course course : matchingCourses) {
                    if (coursesForRequirement.size() >= requirementCountMap.get(requirement)) {
                        break;
                    }

                    coursesForRequirement.add(course);
                    locallyAssignedCourses.add(course); 
                    courseUsageMap.put(course, courseUsageMap.getOrDefault(course, 0) + 1); 
                }
            }
        }

        return requirementCourseMap;
    }

    private static List<Course> getStudentCourses(Summary summary) {
        List<Course> courseList = new ArrayList<>();

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

    private static String normalize(String courseCode) {
        return courseCode.replaceAll("\\s", "").toUpperCase();
    }

    private static boolean courseMatchesRequirement(Course course, Requirement requirement, List<PlanList> planLists) {
        String normalizedCourseCode = normalize(course.getSbj_list() + course.getCnbr_name());
        String normalizedRequirementCode = normalize(requirement.getSbj_list() + requirement.getCnbr_name());

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

        return normalizedCourseCode.equals(normalizedRequirementCode);
    }

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