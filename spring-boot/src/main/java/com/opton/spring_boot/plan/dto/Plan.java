package com.opton.spring_boot.plan.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Plan { 
    private final String name;
    private final int year;
    private final List<Category> categoryList = new ArrayList<>();

    public void add(String categoryName, Requirement requirement) {
        Category category = categoryList.stream()
                .filter(cat -> cat.getName().equals(categoryName))
                .findFirst()
                .orElseGet(() -> {
                    Category newCategory = new Category(categoryName);
                    categoryList.add(newCategory);
                    return newCategory;
                });
        category.add(requirement);
    }

    public Iterator<Category> getCategoryIterator() {
        return categoryList.iterator();
    }

    /**
     * Returns number of plan requirements.
     * @return number of plan requirements.
     */
    public int size() {
        return categoryList.stream().mapToInt(cat -> cat.getRequirementList().size()).sum();
    }
}