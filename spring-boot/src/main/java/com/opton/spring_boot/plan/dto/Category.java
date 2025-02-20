package com.opton.spring_boot.plan.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Iterator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Course requirements are grouped into named categories. A category
 * maintains a list of matches of course to requirement.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class Category implements Comparable<Category> {
    private final String name;
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