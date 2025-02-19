package com.opton.spring_boot.audit;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.opton.spring_boot.audit.dto.Course;
import com.opton.spring_boot.plan.dto.Category;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;

import lombok.Getter;

/**
 * Audit class records a student audit for a given plan. The status of the audit
 * can be retrieved along with a list of courses matching each requirement.
 */
@Getter
public class Audit {
    /**
     * Audit status. Unknown if no audit done. Provisionally_Complete
     * means that it will be complete if all in-progress courses are
     * passed.
     */
    public enum Status { Unknown, Incomplete, Provisionally_Complete, Complete }

    // given as parameter at construction
    private final Plan plan;

    // list of courses matching each requirement - set by constructor
    private final Map<Requirement, List<Course>> requirementCourseListMap;

    // maps statuses to plan categories - calculated during construction
    private final Map<Category, Status> categoryStatusMap;

    // overall status of the audit - calculated during construction
    private final Status overallStatus;

    /**
     * Constructor copies parameters, initializes the status map, and
     * calculates the status for each category and the overall audit.
     * @param plan to be audited for
     * @param requirementCourseListMap map of requirements to matched courses
     */
    public Audit(Plan plan, Map<Requirement, List<Course>> requirementCourseListMap) {
        this.plan = plan;
        this.requirementCourseListMap = requirementCourseListMap;
        this.categoryStatusMap = new TreeMap<>();

        // Initialize category statuses and calculate overall status
        this.overallStatus = calculateStatuses();
    }

    /**
     * Calculate the status for each category and the overall audit.
     * @return the overall status of the audit
     */
    private Status calculateStatuses() {
        Status overallStatus = Status.Complete;

        // Iterate through each category and calculate its status
        Iterator<Category> cItr = plan.getCategoryIterator();
        while (cItr.hasNext()) {
            Category category = cItr.next();
            Status categoryStatus = calculateCategoryStatus(category);
            categoryStatusMap.put(category, categoryStatus);

            // Update overall status if this category's status is worse
            if (categoryStatus.ordinal() < overallStatus.ordinal()) {
                overallStatus = categoryStatus;
            }
        }

        return overallStatus;
    }

    /**
     * Calculate the status for a specific category.
     * @param category the category to calculate the status for
     * @return the status of the category
     */
    private Status calculateCategoryStatus(Category category) {
        Status categoryStatus = Status.Complete;

        // Iterate through each requirement in the category
        Iterator<Requirement> rItr = category.getRequirementIterator();
        while (rItr.hasNext()) {
            Requirement requirement = rItr.next();
            List<Course> courseList = requirementCourseListMap.get(requirement);
            Status requirementStatus = calculateRequirementStatus(courseList);

            // Update category status if this requirement's status is worse
            if (requirementStatus.ordinal() < categoryStatus.ordinal()) {
                categoryStatus = requirementStatus;
            }
        }

        return categoryStatus;
    }

    /**
     * Calculate the status for a specific requirement.
     * @param courseList the list of courses matching the requirement
     * @return the status of the requirement
     */
    private Status calculateRequirementStatus(List<Course> courseList) {
        if (courseList.isEmpty()) {
            return Status.Incomplete;
        } else {
            for (Course course : courseList) {
                if (course.inProgress()) {
                    return Status.Provisionally_Complete;
                }
            }
            return Status.Complete;
        }
    }

    /**
     * Return list of courses (usually length 1 or 0, sometimes more)
     * matching the given requirement.
     * @param requirement the requirement to return matched courses for
     * @return list of courses matched to the requirement
     */
    public List<Course> requirementCourseList(Requirement requirement) {
        return requirementCourseListMap.get(requirement);
    }

    /**
     * Return the status of the audit for the given plan category.
     * @param category of plan
     * @return the status of the category
     */
    public Status status(Category category) {
        return categoryStatusMap.get(category);
    }
}
