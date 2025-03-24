package com.opton.spring_boot.audit;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Course implements Comparable<Course> {
    private String sbj_list;
    private String cnbr_name;
    public Priority priority;

    public Course(String courseName, String priorityStr) {
        String[] parts = courseName.split(" ", 2);
        this.sbj_list = parts[0];
        this.cnbr_name = parts[1];
        this.priority = getPriority(priorityStr);
    }

    private Priority getPriority(String priorityStr) {
        if (priorityStr.equals("CR")) {
            return Priority.Passed;
        } else if (priorityStr.equals("In Progress") || priorityStr.equals("NG")) {
            return Priority.InProgress;
        } else if (priorityStr.equals("NC")) {
            return Priority.Failed;
        } else {
            try {
                int value = Integer.parseInt(priorityStr);
                if (value >= 50) {
                    return Priority.Passed;
                } else {
                    return Priority.Failed;
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid priority string: " + priorityStr);
            }
        }

    }

    @Override
    public int compareTo(Course other) {
        int result = this.sbj_list.compareTo(other.sbj_list);
        return result != 0 ? result : this.cnbr_name.compareTo(other.cnbr_name);
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.sbj_list, this.cnbr_name).toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(sbj_list, course.sbj_list) &&
                Objects.equals(cnbr_name, course.cnbr_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sbj_list, cnbr_name);
    }
}
