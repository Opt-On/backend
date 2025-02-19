package com.opton.spring_boot.audit;

import java.util.Arrays; // Import the Arrays class

class CourseAllocator {
    private int[][] matrix; // Matrix to store course-requirement priorities
    private int[] assignment; // Stores the best course index for each requirement
    private int numCourses;
    private int numRequirements;

    /**
     * Constructor for CourseAllocator.
     *
     * @param numCourses      The number of courses.
     * @param numRequirements The number of requirements.
     */
    public CourseAllocator(int numCourses, int numRequirements) {
        this.numCourses = numCourses;
        this.numRequirements = numRequirements;
        this.matrix = new int[numCourses][numRequirements];
        this.assignment = new int[numRequirements];
        Arrays.fill(assignment, -1); // Initialize with no assignments
    }

    /**
     * Sets the priority for a course-requirement pair.
     *
     * @param courseIndex The index of the course.
     * @param rqmtIndex   The index of the requirement.
     * @param priority    The priority of the match.
     */
    public void set(int courseIndex, int rqmtIndex, int priority) {
        matrix[courseIndex][rqmtIndex] = priority;
    }

    /**
     * Solves the assignment problem to maximize priority coverage.
     */
    public void solve() {
        // Implement a matching algorithm here, such as the Hungarian algorithm or a greedy approach.
        // For simplicity, this example uses a greedy approach.
        for (int rqmtIndex = 0; rqmtIndex < numRequirements; rqmtIndex++) {
            int maxPriority = -1;
            int bestCourseIndex = -1;

            for (int courseIndex = 0; courseIndex < numCourses; courseIndex++) {
                if (matrix[courseIndex][rqmtIndex] > maxPriority) {
                    maxPriority = matrix[courseIndex][rqmtIndex];
                    bestCourseIndex = courseIndex;
                }
            }

            if (bestCourseIndex != -1) {
                assignment[rqmtIndex] = bestCourseIndex;
            }
        }
    }

    /**
     * Returns the best course index for a given requirement.
     *
     * @param rqmtIndex The index of the requirement.
     * @return The index of the best course, or -1 if no course is assigned.
     */
    public int firstRow(int rqmtIndex) {
        return assignment[rqmtIndex];
    }
}