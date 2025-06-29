/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.controller;

import com.audit.myexpense.model.DayWiseExpense;
import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.MonthlySummary;
import com.audit.myexpense.model.MonthlyTarget;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

/**
 * Controller for monthly summary
 *  @author Manikandan Narasimhan
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class MonthlySummaryController {

   final List<String> monthList = Arrays.asList("January", "February", "March", "April",
            "May", "June", "July", "August", "September",
            "October", "November", "December");

    private final MongoTemplate mongoTemplate;
    public MonthlySummaryController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    /**
     * @param year  year of summary
     * @param month month of summary
     * @return monthlySummary
     */
    @GetMapping("/monthlySummary")
    public List<MonthlySummary> monthlySummary(@RequestParam Integer year,
                                               @RequestParam(required = false) String month) {
        List<MonthlySummary> response = new ArrayList<>();
        final List<Criteria> criteria = new ArrayList<>();
        if (year != null) {
            criteria.add(Criteria.where("year").is(year));
        }
        if (month != null && !month.isEmpty()) {
            criteria.add(Criteria.where("month").is(month));
        }

        Map<String,MonthlySummary> expenseSummaryMap = aggregationResults(criteria,"expense","myExpenseDetail")
                .getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear()+exp.getMonth(), exp->exp));

        Map<String,MonthlySummary> incomeSummaryMap =  aggregationResults(criteria,"income","myIncomeDetail")
                .getMappedResults().stream().collect(Collectors.toMap(exp->exp.getYear()+exp.getMonth(),exp->exp));
        // estimate datas
        final Query query =  new Query(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));
        List<MonthlyTarget> result = mongoTemplate.find(query, MonthlyTarget.class);
        for(MonthlySummary summary :expenseSummaryMap.values() ){
            MonthlySummary incomeDetail = incomeSummaryMap.get(summary.getYear()+summary.getMonth());
            summary.setIncome(Math.round(incomeDetail!=null?incomeDetail.getIncome():0.0));
            summary.setSavings(Math.round(incomeDetail!=null?incomeDetail.getIncome()-summary.getExpense():0.0-summary.getExpense()));
            summary.setEstimated(Math.round(result.stream().filter(estimate -> estimate.year== summary.getYear() &&
                    estimate.month.equals(summary.getMonth())).mapToDouble(t->t.amount).sum()));
            summary.setExpense(Math.round(summary.getExpense()));
            response.add(summary);
        }
        for(MonthlySummary incomeSummary :incomeSummaryMap.values()) {
            if(expenseSummaryMap.get(incomeSummary.getYear()+incomeSummary.getMonth()) == null) {
                incomeSummary.setSavings(Math.round(incomeSummary.getIncome()));
                incomeSummary.setEstimated(Math.round(result.stream().filter(estimate -> estimate.year== incomeSummary.getYear() &&
                        estimate.month.equals(incomeSummary.getMonth())).mapToDouble(t->t.amount).sum()));
                response.add(incomeSummary);
            }
        }
        response.sort((obj1, obj2) -> {
            if (monthList.indexOf(obj1.getMonth()) > monthList.indexOf(obj2.getMonth())) {
                return -1;
            } else if (monthList.indexOf(obj1.getMonth()) < monthList.indexOf(obj2.getMonth())) {
                return 1;
            }
            return 0;
        });
        return response;
    }

    private AggregationResults<MonthlySummary> aggregationResults(List<Criteria> criteria, String attribute, String collectionName) {
        Aggregation agg = newAggregation(
                match( new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()]))),
                group("year","month").sum("amount").as(attribute),
                project(attribute,"month","year")
        );
        return mongoTemplate.aggregate(agg,collectionName ,MonthlySummary.class);
    }

    @GetMapping("/dailySummary")
    public List<DayWiseExpense> dailySummary(@RequestParam Integer year,
                                               @RequestParam String month) {

        int monthIndex = monthList.indexOf(month); // Get the month index (0-based)
        if (monthIndex == -1) {
            throw new IllegalArgumentException("Invalid month name: " + month);
        }

        // Convert month index to 1-based (required for YearMonth)
        monthIndex += 1;

        // Create YearMonth object for the given year and month
        YearMonth yearMonth = YearMonth.of(year, monthIndex);

        // Get the start and end dates of the given month
        LocalDate startDate = yearMonth.atDay(1); // First day of the month
        LocalDate endDate = yearMonth.atEndOfMonth(); // Last day of the month

        // Query to fetch expenses for the given month
        Query query = new Query(Criteria.where("expenseDate")
                .gte(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .lte(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())));
        List<ExpenseDetails> expenses = mongoTemplate.find(query, ExpenseDetails.class, "myExpenseDetail");

        // Format the expenseDate to YYYY-MM-DD and group by date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        // Group expenses by date and calculate the sum for each day
        Map<String, Double> expenseMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.expenseDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter),
                        Collectors.summingDouble(b-> b.amount)
                ));

        // Generate a list of DayWiseExpense for all days in the month
        List<DayWiseExpense> dayWiseExpenses = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            String date = yearMonth.atDay(day).toString(); // Format: YYYY-MM-DD
            double expense = expenseMap.getOrDefault(date, 0.0); // Default to 0 if no expense
            dayWiseExpenses.add(new DayWiseExpense(date, expense));
        }

        return dayWiseExpenses;
    }

    }
