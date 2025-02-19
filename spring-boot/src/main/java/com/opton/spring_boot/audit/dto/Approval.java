package com.opton.spring_boot.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Approval {
    private int uw_id;
    private String subject;
    private String catalog_nbr;
    private String sbj_list;
    private String cnbr_name;
    private String given_on;
    private String given_by;

    public String getOrigNameString() {
        return sbj_list + cnbr_name;
    }

    public String getSubstNameString() {
        return subject + catalog_nbr;
    }
}

