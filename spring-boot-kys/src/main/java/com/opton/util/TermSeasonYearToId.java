package com.opton.util;

public class TermSeasonYearToId {

    public static int termSeasonYearToId(String maybeSeason, String maybeYear) throws IllegalArgumentException {
        int month;
        switch (maybeSeason) {
            case "Fall":
                month = 9;
                break;
            case "Spring":
                month = 5;
                break;
            case "Winter":
                month = 1;
                break;
            default:
                System.out.println(maybeSeason + "not season");
                throw new IllegalArgumentException("Not a season: " + maybeSeason);
        }

        int year;
        try {
            year = Integer.parseInt(maybeYear);
        } catch (NumberFormatException e) {
            System.out.println("not year");
            throw new IllegalArgumentException("Not a year: " + maybeYear);
        }

        return (year - 1900) * 10 + month;
    }

    public static void main(String[] args) {
        // Example usage
        try {
            int id = termSeasonYearToId("Fall", "2023");
            System.out.println("Generated ID: " + id);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}