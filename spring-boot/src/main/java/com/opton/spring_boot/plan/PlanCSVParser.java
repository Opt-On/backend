package com.opton.spring_boot.plan;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.opton.spring_boot.plan.dto.Category;
import com.opton.spring_boot.plan.dto.ListItem;
import com.opton.spring_boot.plan.dto.Plan;
import com.opton.spring_boot.plan.dto.PlanList;
import com.opton.spring_boot.plan.dto.Requirement;

@Getter
@Setter
public class PlanCSVParser {
    private List<Plan> plans = new ArrayList<>();
    private List<PlanList> lists = new ArrayList<>();

    /**
     * Add plan.
     * @param plan plan to add
     */
    public void add(Plan plan) {
        plans.add(plan);
    }

    /**
     * Add planList.
     * @param planList planList to add
     */
    public void add(PlanList list) {
        lists.add(list);
    }

    /**
     * Imports plans and lists from a CSV.
     * @param reader Reader for the CSV file containing plan/list descriptions
     */
    public void csvIn(Reader reader) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            String curType = "";
            String curCategory = "";
            
            // Skip the header line
            bufferedReader.readLine();

            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",", -1); // Split by comma, preserving empty fields

                if (values.length < 6) {
                    continue; // Skip invalid lines
                }

                String type = values[0].trim();
                if (!type.isEmpty()) {
                    curType = type;
                    String name = values[1].trim();
                    int calendar = Integer.parseInt(values[2].trim());

                    if (type.equals("list")) {
                        lists.add(new PlanList(name, calendar));
                    } else if (type.equals("plan")) {
                        plans.add(new Plan(name, calendar));
                    }
                }

                String sbj_list = values[4].trim();
                String cnbr_name = values[5].trim();

                if (curType.equals("list")) {
                    lists.get(lists.size() - 1).add(sbj_list, cnbr_name);
                } else if (curType.equals("plan")) {
                    String category = values[3].trim();
                    if (!category.isEmpty()) {
                        curCategory = category;
                    }
                    plans.get(plans.size() - 1).add(curCategory, new Requirement(sbj_list, cnbr_name));
                }
            }
        }
    }

    /**
     * Exports plans and lists to CSV (string).
     * @return String representation of the CSV
     */
    public String csvOut() {
        StringBuilder csv = new StringBuilder();
        csv.append("type,name,calendar,category,sbj_list,cnbr_name\n");

        // Export plans
        for (Plan plan : plans) {
            String rowType = "plan";
            String rowName = plan.getName();
            String rowCal = Integer.toString(plan.getCalendar());
            String rowCat = "";
            for (Category c : plan.getCategoryList()) {
                rowCat = c.getName();
                for (Requirement r : c.getRequirementList()) {
                    csv.append(rowType).append(","); rowType = "";
                    csv.append(rowName).append(","); rowName = "";
                    csv.append(rowCal).append(","); rowCal = "";
                    csv.append(rowCat).append(","); rowCat = "";
                    csv.append(r.getSbj_list()).append(",");
                    csv.append(r.getCnbr_name()).append("\n");
                }
            }
        }

        // Export lists
        for (PlanList list : lists) {
            String rowType = "list";
            String rowName = list.getName();
            String rowCal = Integer.toString(list.getCalendar());
            String rowCat = "";
            for (ListItem li : list.getItems()) {
                csv.append(rowType).append(","); rowType = "";
                csv.append(rowName).append(","); rowName = "";
                csv.append(rowCal).append(","); rowCal = "";
                csv.append(rowCat).append(","); rowCat = "";
                csv.append(li.getSbj_list()).append(",");
                csv.append(li.getCnbr_name()).append("\n");
            }
        }

        return csv.toString();
    }
}
