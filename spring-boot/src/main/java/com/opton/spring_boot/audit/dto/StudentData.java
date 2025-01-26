package com.opton.spring_boot.audit.dto;

import java.util.List;

import com.opton.spring_boot.course.dto.Course;

import lombok.Data;

@Data
public class StudentData {
    private List<Course> courses;
}
