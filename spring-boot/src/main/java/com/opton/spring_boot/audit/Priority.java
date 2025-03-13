package com.opton.spring_boot.audit;

public enum Priority {
    Failed, // Excluded from matching
    InProgress, // Lowest priority
    Passed // Highest priority
}
