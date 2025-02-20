package com.opton.spring_boot.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Approval {
    private Course original;
    private Course approved;
}

