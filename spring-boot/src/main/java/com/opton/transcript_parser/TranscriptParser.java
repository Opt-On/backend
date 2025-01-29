package com.opton.transcript_parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import com.opton.transcript_parser.types.TermSummary;
import com.opton.util.TermSeasonYearToId;


public class TranscriptParser {
    private static Pattern courseRegex = Pattern.compile("([A-Z]{2,})\\x20{2,}(\\d{1,3}\\w*)\\x20{2,}.*\\n");
    private static Pattern creditRegex = Pattern.compile("\\d\\.\\d{2}");
    private static Pattern levelRegex = Pattern.compile("Level:\\s+(\\w{2,})");
    private static Pattern studentIdRegex = Pattern.compile("Student ID:\\s+(\\d+)");
    private static Pattern termRegex = Pattern.compile("(?m)^\\s*(Fall|Winter|Spring)\\s+(\\d{4})\\s*$");

    public static boolean IsTransferCredit(String courseLine){
        Matcher regex = courseRegex.matcher(courseLine);
        return regex.matches();
    }

    public static List<int[]> findAllStringSubmatchIndex(Matcher matcher) {
        List<int[]> results = new ArrayList<>();

        while (matcher.find()) {
            // Create an array to store the start and end indices of the full match and capturing groups
            int[] matchIndices = new int[matcher.groupCount() * 2 + 2];
            matchIndices[0] = matcher.start(); // Start index of the full match
            matchIndices[1] = matcher.end();   // End index of the full match

            // Add start and end indices of capturing groups
            for (int i = 1; i <= matcher.groupCount(); i++) {
                matchIndices[i * 2] = matcher.start(i); // Start index of the capturing group
                matchIndices[i * 2 + 1] = matcher.end(i); // End index of the capturing group
            }

            results.add(matchIndices);
        }

        return results;
    }

    // todo: not void
    public static void ParseTranscript(MultipartFile file) throws Exception{
        String transcriptData = PDFToText(file);
        extractTermSummaries(transcriptData);

    }
    
    @SuppressWarnings("null")
    public static String PDFToText(MultipartFile file) throws Exception{
        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".pdf")) {
            System.err.println("invalid pdf transcript uploaded");
            throw new Exception("bad file");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            System.err.println(text);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("failed to parse pdf");
            throw new Exception("can't parse");
        }
    }

    public static TermSummary extractTermSummaries(String text) throws Exception{
        Matcher termMatcher = termRegex.matcher(text);
        Matcher levelMatcher = levelRegex.matcher(text);

        // iterate through the matches, if len not same throw exception
        List <int[]> termList = findAllStringSubmatchIndex(termMatcher);
        List <int[]> levelList = findAllStringSubmatchIndex(levelMatcher);

        if (termList.size() != levelList.size()){
            throw new Exception("num terms != num levels");
        }

        Matcher courseMatcher = courseRegex.matcher(text);
        ArrayList<String> courseList = new ArrayList<>();
        
        while (courseMatcher.find()){
            courseList.add(courseMatcher.group());
        }

        int j = 0;
        for (int i = 0; i < termList.size(); i++){
            String season = text.substring(termList[i][0], termList[i][1]);
            String year = termList.get(i).substring(4,5);
            System.out.println("season"+season+"year"+year);
            int termCode = TermSeasonYearToId.termSeasonYearToId(season, year);

            System.out.println(termCode);
        }

        return new TermSummary();

    } 
}
