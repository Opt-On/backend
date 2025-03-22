package com.opton.spring_boot.audit;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.opton.spring_boot.plan.dto.Category;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.Requirement;

import lombok.Getter;

@Getter
public class Audit {

    public enum Status {
        Unknown, Incomplete, Provisionally_Complete, Complete
    }

    private final Plan plan;

    private final Map<Requirement, List<Course>> requirementCourseListMap;

    private final Map<Category, Status> categoryStatusMap;

    private final Status overallStatus;

    public Audit(Plan plan, Map<Requirement, List<Course>> requirementCourseListMap) {
        this.plan = plan;
        this.requirementCourseListMap = requirementCourseListMap;
        this.categoryStatusMap = new TreeMap<>();

        this.overallStatus = calculateStatuses();
    }

    private Status calculateStatuses() {
        Status overallStatus = Status.Complete;

        Iterator<Category> cItr = plan.getCategoryIterator();
        while (cItr.hasNext()) {
            Category category = cItr.next();
            Status categoryStatus = calculateCategoryStatus(category);
            categoryStatusMap.put(category, categoryStatus);

            if (categoryStatus.ordinal() < overallStatus.ordinal()) {
                overallStatus = categoryStatus;
            }
        }

        return overallStatus;
    }

    public double[] calculateProgress() {
        double completedCourses = 0;

        Map<Requirement, List<Course>> requirementCourseListMap = this.requirementCourseListMap;
        for (Map.Entry<Requirement, List<Course>> entry : requirementCourseListMap.entrySet()) {
            List<Course> courseList = entry.getValue();

            for (Course course : courseList) {
                if (course.getPriority() != Priority.Failed) {
                    completedCourses++;
                }
            }
        }

        return new double[] { completedCourses, this.plan.size() };
    }

    private Status calculateCategoryStatus(Category category) {
        Status categoryStatus = Status.Complete;

        Iterator<Requirement> rItr = category.getRequirementIterator();
        while (rItr.hasNext()) {
            Requirement requirement = rItr.next();
            List<Course> courseList = requirementCourseListMap.get(requirement);
            Status requirementStatus = calculateRequirementStatus(courseList);

            if (requirementStatus.ordinal() < categoryStatus.ordinal()) {
                categoryStatus = requirementStatus;
            }
        }

        return categoryStatus;
    }

    private Status calculateRequirementStatus(List<Course> courseList) {
        if (courseList == null || courseList.isEmpty()) {
            return Status.Incomplete;
        } else {
            return Status.Complete;
        }
    }

    public List<Course> requirementCourseList(Requirement requirement) {
        return requirementCourseListMap.get(requirement);
    }

    public Status status(Category category) {
        return categoryStatusMap.get(category);
    }
}