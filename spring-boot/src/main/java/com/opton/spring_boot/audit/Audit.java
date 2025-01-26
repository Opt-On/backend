package com.opton.spring_boot.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.opton.spring_boot.course.dto.Course;
import com.opton.spring_boot.plan.dto.Category;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;

/**
 * Audit class records a student audit for a given plan. The status of the audit
 * can be retrieved along with a list of courses matching each requirement.
 */
@Getter
@RequiredArgsConstructor
@ToString
public class Audit {

    public enum Status { Unknown, Incomplete, Provisionally_Complete, Complete }

    private final Plan plan;
    private final Map<Requirement, List<Course>> requirementCourseListMap;
    private final Map<Category, Status> categoryStatusMap;  

    /**
     * Constructor initializes the audit for the given plan and requirement list.
     * @param plan to be audited
     * @param requirementCourseListMap mapping of requirements to matching courses
     */
    public Audit(Plan plan, Map<Requirement, List<Course>> requirementCourseListMap) {
        this.plan = plan;
        this.requirementCourseListMap = requirementCourseListMap;
        this.categoryStatusMap = new HashMap<>();  

        // Initialize category statuses to Unknown
        for (Category category : plan.getCategoryList()) { 
            categoryStatusMap.put(category, Status.Unknown);
        }
    }

    /**
     * Returns a list of courses that satisfy a given requirement.
     * @param requirement the requirement to return matched courses for
     * @return list of courses matching the requirement
     */
    public List<Course> requirementCourseList(Requirement requirement) {
        return Optional.ofNullable(requirementCourseListMap.get(requirement))
                .orElse(Collections.emptyList());
    }

    /**
     * Returns the overall status of the audit.
     * @return status of the audit
     */
    public Status status() {
        return plan.getCategoryList().stream()
                .map(this::status) 
                .min(Comparator.naturalOrder()) 
                .orElse(Status.Complete); 
    }

    /**
     * Returns the status of a specific category in the audit.
     * @param category the category to return the status for
     * @return status of the category
     */
    public Status status(Category category) {
        Status categoryStatus = categoryStatusMap.get(category);

        if (categoryStatus == Status.Unknown) {
            categoryStatus = calculateCategoryStatus(category);
            categoryStatusMap.put(category, categoryStatus);
        }

        return categoryStatus;
    }

    /**
     * Calculates the status for a given category.
     * @param category the category to calculate status for
     * @return the calculated status of the category
     */
    private Status calculateCategoryStatus(Category category) {
        Status categoryStatus = Status.Complete;
        
        for (Requirement requirement : category.getRequirementList()) {
            List<Course> courseList = requirementCourseList(requirement);

            Status requirementStatus = courseList.isEmpty() ? Status.Incomplete : Status.Complete;
            for (Course course : courseList) {
                if (course.inProgress()) {
                    requirementStatus = Status.Provisionally_Complete;
                    break;
                }
            }
            
            categoryStatus = requirementStatus.compareTo(categoryStatus) < 0 ? requirementStatus : categoryStatus;
        }

        return categoryStatus;
    }
}
