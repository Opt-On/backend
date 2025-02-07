package com.opton.spring_boot.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Iterator;
import lombok.Data;

/**
 * Course requirements are grouped into named categories. A category
 * maintains a list of matches of course to requirement.
 */
@Data
public class Category implements Comparable<Category> {
    private String name;
    private List<Requirement> requirementList = new ArrayList<>();

    /**
     * Add a requirement with null course to matches.
     * @param requirement requirement
     */
    public void add(Requirement requirement) {
        requirementList.add(requirement);
    }

    @Override
    public int compareTo(Category category) {
        return this.name.compareTo(category.name);
    }

    public Iterator<Requirement> getRequirementIterator() {
        return requirementList.iterator();
    }

    @Override
    public String toString() {
        return String.format("{%s,[%s]}", 
            name, 
            requirementList.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))
        );
    }

}
