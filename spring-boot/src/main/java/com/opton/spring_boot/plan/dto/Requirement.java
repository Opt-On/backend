package com.opton.spring_boot.plan.dto;

import lombok.Getter;

@Getter
public class Requirement implements Comparable<Requirement> {
    private String sbj_list;
    private String cnbr_name;
    private int number = -1;

    public Requirement(String sbj_list, String cnbr_name) {
        this.sbj_list = sbj_list;
        this.cnbr_name = cnbr_name;
    }

    @Override
    public int compareTo(Requirement requirement) {
        return this.number - requirement.number;
    }

    @Override
    public String toString() {
        if ("list".equals(sbj_list)) {  
            int sepIndex = cnbr_name.indexOf("_");
            if (sepIndex >= 0) {
                String plan = cnbr_name.substring(0, sepIndex).toUpperCase();
                String list = cnbr_name.substring(sepIndex + 1);
                return String.format("%s %s list", plan, list);
            } else {
                return cnbr_name + " list";
            }
        } else {
            return sbj_list + cnbr_name; 
        }
    }

    /**
     * Checks if this requirement matches the given list item.
     *
     * @param item The list item to check.
     * @return True if the requirement matches the list item, false otherwise.
     */
    public boolean matches(ListItem item) {
        return this.sbj_list.equals(item.getSbj_list()) &&
               this.cnbr_name.equals(item.getCnbr_name());
    }
}