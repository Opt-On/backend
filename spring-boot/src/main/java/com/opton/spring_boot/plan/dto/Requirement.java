package com.opton.spring_boot.plan.dto;

import java.util.Objects;

import lombok.Getter;

@Getter

public class Requirement implements Comparable<Requirement> {
    private String sbj_list;
    private String cnbr_name;

    public Requirement(String sbj_list, String cnbr_name) {
        this.sbj_list = sbj_list;
        this.cnbr_name = cnbr_name;
    }

    @Override
    public int compareTo(Requirement requirement) {
        return this.cnbr_name.compareTo(requirement.cnbr_name);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Requirement that = (Requirement) o;
        return Objects.equals(sbj_list, that.sbj_list) &&
               Objects.equals(cnbr_name, that.cnbr_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sbj_list, cnbr_name);
    }
}