package com.opton.spring_boot.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Getter;

import java.util.Set;
import java.util.TreeSet;
import java.util.Comparator;

@Data
@RequiredArgsConstructor
@Getter
@ToString
public class PlanList implements Comparable<PlanList> {
    private final String name;
    private final int calendar;
    private final Set<ListItem> items = new TreeSet<>(); // Lists don't have duplicate items

    /**
     * Compares this PlanList to another for ordering.
     *
     * @param other The other PlanList to compare against.
     * @return Comparison result.
     */
    @Override
    public int compareTo(PlanList other) {
        return Comparator.comparing(PlanList::getName)
                .thenComparing(PlanList::getCalendar)
                .compare(this, other);
    }

    /**
     * Adds a new ListItem to the PlanList.
     *
     * @param sbj_list The subject list of the item.
     * @param cnbr_name The course number name of the item.
     */
    public void add(String sbj_list, String cnbr_name) {
        items.add(new ListItem(sbj_list, cnbr_name));
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
