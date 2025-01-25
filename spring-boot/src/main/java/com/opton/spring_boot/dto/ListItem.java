package com.opton.spring_boot.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public  class ListItem implements Comparable<ListItem> {
    private final String sbj_list; 
    private final String cnbr_name; 

    @Override
    public int compareTo(ListItem other) {
        int result = this.sbj_list.compareTo(other.sbj_list);
        return result != 0 ? result : this.cnbr_name.compareTo(other.cnbr_name);
    }
}
