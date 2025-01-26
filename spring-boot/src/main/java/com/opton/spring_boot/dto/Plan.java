package com.opton.spring_boot.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Data
@RequiredArgsConstructor
public class Plan {

    @Getter
    @RequiredArgsConstructor
    @ToString
    public class Category implements Comparable<Category> {
        private final String name;
        private final List<Requirement> requirementList = new ArrayList<>();

        /**
         * Add a requirement with null course to matches.
         * @param requirement requirement
         */
        public void add(Requirement requirement) {
            requirementList.add(requirement);
        }

        @Override
        public int compareTo(Category other) {
            return this.name.compareTo(other.name);
        }

        public Iterator<Requirement> getRequirementIterator() {
            return requirementList.iterator();
        }
    }

    private final String name;
    private final int calendar;
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
