package com.opton.spring_boot.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Transcript {
    public static final String ENTITY_NAME = "transcript"; // set to something else ??

    private String programName;
    private int studentId;
}
