package com.opton.spring_boot;

import java.io.FileReader;
import java.io.IOException;

import com.opton.spring_boot.plan.PlanCSVParser;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.PlanList;

public class PlanCSVParserTest {

    public static void main(String[] args) {
        String filePath = "./spring-boot/src/test/java/com/opton/spring_boot/ME2023.csv";
        PlanCSVParser parser = new PlanCSVParser();

        try (FileReader fileReader = new FileReader(filePath)) {
            parser.csvIn(fileReader);
            
            System.out.println("Plans:");
            for (Plan plan : parser.getPlans()) {
                System.out.println(plan);
            }

            System.out.println("Plan Lists:");
            for (PlanList list : parser.getLists()) {
                System.out.println(list);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
