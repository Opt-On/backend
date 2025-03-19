package com.opton.spring_boot.transcript_parser;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import com.opton.spring_boot.transcript_parser.types.Summary;
import com.opton.spring_boot.transcript_parser.types.TermSummary;
import com.opton.util.TermSeasonYearToId;

public class TranscriptParser {
    private static Pattern courseRegex = Pattern.compile("([A-Z]{2,})\\x20{2,}(\\d{1,3}\\w*)\\x20{1,}.*\\n");
    private static Pattern creditRegex = Pattern.compile("\\d\\.\\d{2}");
    private static Pattern levelRegex = Pattern.compile("Level:\\s+(\\w{2,})");
    private static Pattern studentIdRegex = Pattern.compile("Student ID:\\s+(\\d+)");
    private static Pattern termRegex = Pattern.compile("(?m)^\\s*(Fall|Winter|Spring)\\s+(\\d{4})\\s*$");
    private static Pattern studentNameRegex = Pattern.compile("Name:\\s+([^\\n]+)");
    private static Pattern courseResultRegex = Pattern
            .compile("([A-Z]{2,})\\s{2,}(\\d{1,3}\\w*)\\s+.*?(\\d\\.\\d{2})\\s*(\\d\\.\\d{2})\\s*(\\d{1,3}|CR|NC|NG)");
    private static Pattern completedCourseRegex = Pattern
            .compile("([A-Z]{2,})\\s{2,}(\\d{1,3}\\w*)\\s{1,}.*(\\d\\.\\d{2}).*\\n");

    public static boolean IsTransferCredit(String courseLine) {
        Matcher regex = courseRegex.matcher(courseLine);
        return regex.matches();
    }

    public static List<int[]> findAllStringSubmatchIndex(Matcher matcher) {
        List<int[]> results = new ArrayList<>();

        while (matcher.find()) {
            // Create an array to store the start and end indices of the full match and
            // capturing groups
            int[] matchIndices = new int[matcher.groupCount() * 2 + 2];
            matchIndices[0] = matcher.start(); // Start index of the full match
            matchIndices[1] = matcher.end(); // End index of the full match

            // Add start and end indices of capturing groups
            for (int i = 1; i <= matcher.groupCount(); i++) {
                matchIndices[i * 2] = matcher.start(i); // Start index of the capturing group
                matchIndices[i * 2 + 1] = matcher.end(i); // End index of the capturing group
            }

            results.add(matchIndices);
        }

        return results;
    }

    public static Summary ParseTranscript(MultipartFile file) throws Exception {
        String transcriptData = PDFToText(file);
        // System.out.println(transcriptData);
        ArrayList<TermSummary> termSummaries = extractTermSummaries(transcriptData);
        int studentNumber = extractStudentNumber(transcriptData);
        String programName = extractProgramName(transcriptData);
        String studentName = extractStudentName(transcriptData);

        Summary summary = new Summary();
        summary.studentNumber = studentNumber;
        summary.termSummaries = termSummaries;
        String[] nameParts = studentName.split(", ");
        summary.lastName = nameParts[0];
        summary.firstName = nameParts.length > 1 ? nameParts[1] : "bozo";

        String[] programParts = programName.trim().split("/\\s*");
        summary.programName = programParts[0];

        if (programParts.length > 1) {
            summary.optionNames = new ArrayList<>(
                    Arrays.asList(Arrays.copyOfRange(programParts, 1, programParts.length)));
        }

        summary.uploadDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        System.out.println(programName);
        System.out.println(studentNumber);
        System.out.println(studentName);

        return summary;
    }

    @SuppressWarnings("null")
    public static String PDFToText(MultipartFile file) throws Exception {
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
            System.err.println("invalid pdf transcript uploaded");
            throw new Exception("bad file");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            // System.err.println(text);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("failed to parse pdf");
            throw new Exception("can't parse");
        }
    }

    // courseLine is of one of the following forms:
    //
    // ECON 102 Macroeconomics 0.50 0.50 98
    // ECON 102 Macroeconomics
    // ECON 102 Macroeconomics 0.50
    //
    // Those are, in order: past term course, current term course, transfer credit.
    // isTransferCredit should return true only for the last case.
    public static boolean isTransferCredit(String courseLine) {
        ArrayList<String> matches = new ArrayList<>();
        Matcher gradeMatcher = creditRegex.matcher(courseLine);
        while (gradeMatcher.find()) {
            matches.add(gradeMatcher.group());
        }
        return matches.size() == 1;
    }

    public static ArrayList<TermSummary> extractTermSummaries(String text) throws Exception {
        Matcher termMatcher = termRegex.matcher(text);
        Matcher levelMatcher = levelRegex.matcher(text);
        Matcher courseMatcher = courseRegex.matcher(text);
        Matcher courseResultMatcher = courseResultRegex.matcher(text);
        Matcher completedCourseMatcher = completedCourseRegex.matcher(text);

        // iterate through the matches, if len not same throw exception
        List<int[]> termMatches = findAllStringSubmatchIndex(termMatcher);
        List<int[]> levelMatches = findAllStringSubmatchIndex(levelMatcher);
        List<int[]> courseMatches = findAllStringSubmatchIndex(courseMatcher);
        List<int[]> courseResultMatches = findAllStringSubmatchIndex(courseResultMatcher);
        List<int[]> completedCoursesMatches = findAllStringSubmatchIndex(completedCourseMatcher);

        if (termMatches.size() != levelMatches.size()) {
            throw new Exception("num terms != num levels");
        }

        ArrayList<TermSummary> termSummaries = new ArrayList<>();

        int j = 0; // courseMatches
        int k = 0; // courseResultMatches
        int l = 0; // completedCoursesMatches
        for (int i = 0; i < termMatches.size(); i++) {
            String season = text.substring(termMatches.get(i)[2], termMatches.get(i)[3]);
            String year = text.substring(termMatches.get(i)[4], termMatches.get(i)[5]);

            int termCode = TermSeasonYearToId.termSeasonYearToId(season, year);
            String level = text.substring(levelMatches.get(i)[2], levelMatches.get(i)[3]);

            TermSummary termSummary = new TermSummary();
            termSummary.level = level;
            termSummary.termId = termCode;
            termSummary.courses = new ArrayList<>();

            for (; j < courseMatches.size()
                    && (i == termMatches.size() - 1 || courseMatches.get(j)[0] < termMatches.get(i + 1)[0]); j++) {
                if (isTransferCredit(text.substring(courseMatches.get(j)[0], courseMatches.get(j)[1]))) {
                    continue;
                }
                // course info
                String department = text.substring(courseMatches.get(j)[2], courseMatches.get(j)[3]);
                String number = text.substring(courseMatches.get(j)[4], courseMatches.get(j)[5]);
                String course = (department + " " + number);
                String grade = "CR";

                if (k < courseResultMatches.size()
                        && text.substring(courseMatches.get(j)[2], courseMatches.get(j)[3])
                                .equals(text.substring(courseResultMatches.get(k)[2], courseResultMatches.get(k)[3]))
                        && text.substring(courseMatches.get(j)[3], courseMatches.get(j)[4])
                                .equals(text.substring(courseResultMatches.get(k)[3], courseResultMatches.get(k)[4]))) {
                    grade = text.substring(courseResultMatches.get(k)[10], courseResultMatches.get(k)[11]);
                    k++;
                }
                // check if course is graded (i.e not in currently in progress)
                if (l < completedCoursesMatches.size()
                        && text.substring(courseMatches.get(j)[2], courseMatches.get(j)[3])
                                .equals(text.substring(completedCoursesMatches.get(l)[2],
                                        completedCoursesMatches.get(l)[3]))) {
                    l++;
                } else {
                    grade = "In Progress";
                }

                Map<String, String> courseMap = new HashMap<>();
                courseMap.put(course, grade);
                termSummary.courses.add(courseMap);
            }
            termSummaries.add(termSummary);
        }

        return termSummaries;

    }

    public static int extractStudentNumber(String text) throws IllegalArgumentException {
        Matcher studentNumberMatcher = studentIdRegex.matcher(text);

        if (!studentNumberMatcher.find()) {
            throw new IllegalArgumentException("no student id");
        }

        String studentNumberStr = studentNumberMatcher.group(1);

        // Convert the student number to an integer
        try {
            return Integer.parseInt(studentNumberStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid student number: " + studentNumberStr);
        }
    }

    public static String extractStudentName(String text) throws IllegalArgumentException {
        Matcher studentNameMatcher = studentNameRegex.matcher(text);

        if (!studentNameMatcher.find()) {
            throw new IllegalArgumentException("no student id");
        }

        String studentNumberStr = studentNameMatcher.group(1);

        return studentNumberStr;
    }

    public static String extractProgramName(String text) throws IllegalArgumentException {
        // Find the start index of "Program:"
        int start = text.lastIndexOf("Program:");
        if (start == -1) {
            throw new IllegalArgumentException("Program name not found");
        }

        // Skip "Program:" to get the start of the program name
        start += 8;

        String target = "Level:";

        // Find the end of the program name (delimited by ',' or '\n')
        for (int end = start; end < text.length(); end++) {
            if (text.startsWith(target, end)) {
                String programName = text.substring(start, end);

                // get it in consistent PROGRAM_NAME/ "OPTION_NAME" format
                if (programName.contains("/")) {
                    programName = programName.replace("/", "/ ");
                }

                programName = programName
                        .replace("\n", "/ ")
                        .replace("Honours", " ")
                        .replace("Co-operative Program", " ")
                        .replace(",", " ")
                        .replaceAll("\\s+", " ") // multi spaces
                        .replaceAll(" /", "/")
                        .trim();
                return programName;
            }
        }

        // If no delimiter is found, throw an error
        throw new IllegalArgumentException("Unexpected end of transcript");
    }

}
