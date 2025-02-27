package com.opton.spring_boot.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.Set;
import java.util.TreeSet;

import com.opton.spring_boot.audit.dto.Course;

import java.util.Comparator;

@Data
@Getter
@AllArgsConstructor
public class PlanList implements Comparable<PlanList> {
    private final String name;
    private final int year;
    private final Set<Course> items = new TreeSet<>(); 

    /**
     * Compares this PlanList to another for ordering.
     *
     * @param other The other PlanList to compare against.
     * @return Comparison result.
     */
    @Override
    public int compareTo(PlanList other) {
        return Comparator.comparing(PlanList::getName)
                .thenComparing(PlanList::getYear)
                .compare(this, other);
    }

    /**
     * Adds a new Course to the PlanList.
     *
     * @param sbj_list The subject list of the item.
     * @param cnbr_name The course number name of the item.
     */
    public void add(String sbj_list, String cnbr_name) {
        items.add(new Course(sbj_list, cnbr_name));
    }

    /**
     * Returns the number of items in the PlanList.
     *
     * @return Number of items.
     */
    public int size() {
        return items.size();
    }
}