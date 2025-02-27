package com.opton.spring_boot.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public  class Course implements Comparable<Course> {
    private String sbj_list; 
    private String cnbr_name; 

    @Override
    public int compareTo(Course other) {
        int result = this.sbj_list.compareTo(other.sbj_list);
        return result != 0 ? result : this.cnbr_name.compareTo(other.cnbr_name);
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.sbj_list, this.cnbr_name);
    }
}
